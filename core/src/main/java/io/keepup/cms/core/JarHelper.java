package io.keepup.cms.core;

import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.datasource.resources.StaticContentDeliveryService;
import io.keepup.cms.core.datasource.resources.StorageType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.keepup.cms.core.datasource.resources.StorageType.FILESYSTEM;
import static io.keepup.cms.core.datasource.resources.StorageType.FTP;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Helper class implementing the logic for routine work with Jar files, e.g. unpacking static content
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Service
public class JarHelper {
    private final Log logger = LogFactory.getLog(getClass());
    private ApplicationConfig applicationConfig;
    private StaticContentDeliveryService contentDeliveryService;

    @Value("${keepup.plugins.threshold-entries:10000}")
    private int thresholdEntries;
    @Value("${keepup.plugins.threshold-size:1000000000}")
    private int thresholdSize;
    @Value("${keepup.plugins.threshold-ratio:10}")
    private double thresholdRatio;

    // region lambda maps
    private final Map<StorageType, Consumer<ResourceFileToCopy>> directoryProcessors = new EnumMap<>(StorageType.class);
    // endregion

    /**
     * Path to inner resource where JAR files in Spring Boot application are stored.
     */
    public static final String JAR_BOOT_INF_LIB = "/BOOT-INF/lib";

    /**
     * Default constructor.
     */
    public JarHelper() {
        directoryProcessors.put(StorageType.FILESYSTEM, new CreateFilesystemDirectory());
    }

    /**
     * Set the link to application config
     *
     * @param applicationConfig application configuration
     */
    @Autowired
    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    /**
     * Set the link to static content delivery service component
     *
     * @param contentDeliveryService static content delivery service
     */
    @Autowired
    public void setContentDeliveryService(StaticContentDeliveryService contentDeliveryService) {
        this.contentDeliveryService = contentDeliveryService;
    }

    /**
     * Unpacks Jar file stored inside of the application to some temp folder, processes it as a KeepUP plugin and then
     * deletes the file from filesystem
     *
     * @param decodedPath decoded path to file inside of another Jar entry
     * @param jarDataFile file containing the needed Jar entry
     * @param pluginName  name of the plugin to be deployed
     * @throws IOException is thrown if something went wrong during IO operations
     */
    public void processPluginFromApplicationLibEntry(String decodedPath, JarFile jarDataFile, String pluginName) throws IOException {
        var tempJarFile = getUnpackedJarFile(decodedPath, jarDataFile, pluginName);
        var innerJarFile = new JarFile(tempJarFile);
        processJarStaticResources(innerJarFile);
        FileUtils.delete(tempJarFile);
    }

    /**
     * Copies inner Jar file to some temp storage inside of the filesystem as such files cannot be unpacked from
     * output streams in memory.
     *
     * @param decodedPath decoded path to file inside of another Jar entry
     * @param jarDataFile file containing the needed Jar entry
     * @param pluginName  name of the plugin to be deployed
     * @return            File in the filesystem with the copy of Jar resource
     * @throws IOException is thrown if something went wrong during IO operations
     */
    @NotNull
    public File getUnpackedJarFile(String decodedPath, JarFile jarDataFile, String pluginName) throws IOException {
        var tempJarFile = new File("%s/%s.jar".formatted(applicationConfig.getDump(), pluginName));
        var pluginJarEntry = jarDataFile.getJarEntry(decodedPath);
        var inputStream = jarDataFile.getInputStream(pluginJarEntry);
        FileUtils.copyInputStreamToFile(inputStream, tempJarFile);
        return tempJarFile;
    }

    /**
     * Performs unpacking of static resources according to the specified rules. Files from META-INF/dump directory
     * of Jar entry will be copied to {@link ApplicationConfig#getDump()} folder, files from directory
     * META-INF/server - to {@link ApplicationConfig#getFtpServer()}, files from directory META-INF/static
     * will be copied to {@link ApplicationConfig#getStaticPath()} folder.
     *
     * @param jarFile      content file that can be read by Java
     * @throws IOException is thrown if something went wrong during IO operations
     */
    public void processJarStaticResources(JarFile jarFile) throws IOException {


        var enumEntries = jarFile.entries();
        while (enumEntries.hasMoreElements()) {

            var file = enumEntries.nextElement();

            if (file.getName().startsWith("META-INF/")) {
                ResourceFileToCopy copyCondition = getCopyCondition(file);
                var copy = copyCondition.copy();
                var resourceFile = copyCondition.resourceFile();
                if (needToExtractFile(copy, resourceFile)) {

                    if (file.isDirectory()) {
                        ofNullable(directoryProcessors.get(copyCondition.storageType()))
                                .ifPresent(processor -> processor.accept(copyCondition));
                        continue;
                    }
                    copyFileToSystem(jarFile, resourceFile, file, copyCondition.storageType());
                }
            }
        }
    }

    private void copyFileToSystem(JarFile jar, File resourceFile, JarEntry file, StorageType storageType) throws IOException {
        FileOutputStream fos = null;
        var parent = resourceFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            logger.error(format("Couldn't create dir: %s", parent));
        }
        try (var is = jar.getInputStream(file)) {
            fos = new FileOutputStream(resourceFile);
            logger.info("Copying resource %s".formatted(resourceFile.getAbsolutePath()));
            while (is.available() > 0) {
                fos.write(is.read());
            }
            if (!FILESYSTEM.equals(storageType)) {
                contentDeliveryService.store(resourceFile, getRelativePath(resourceFile));
            }
        } catch (FileNotFoundException ex) {
            logger.info("File %s was not found, skipping. Exception is: %s".formatted(ex.toString(), resourceFile.getAbsolutePath()));
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        if (!FILESYSTEM.equals(storageType) && resourceFile.exists()) {
            final boolean deleteResult = Files.deleteIfExists(resourceFile.toPath());
            if (!deleteResult) {
                logger.warn("Could not delete file %s".formatted(resourceFile.getAbsolutePath()));
            }
        }
    }

    private ResourceFileToCopy getCopyCondition(JarEntry file) throws IOException {
        var storageType = StorageType.valueOf(applicationConfig.getStorageType());
        File resourceFile;
        var serverFiles = "META-INF/server";
        String filename = file.getName();
        if (filename.startsWith(serverFiles)) {
            if (filename.startsWith("META-INF/server/")) {
                resourceFile = new File("%s/resources%s%s".formatted(applicationConfig.getDocumentRoot(), separator, filename.replace(serverFiles, EMPTY)));
                deleteResourceFileIfExists(resourceFile);
                return new ResourceFileToCopy(resourceFile, true, storageType);
            } else {
                return new ResourceFileToCopy(null, false, storageType);
            }
        } else if (filename.startsWith("META-INF/frontend")) {
            resourceFile = new File("%s%s%s".formatted(applicationConfig.getStaticPath(), separator, filename.replace("META-INF/frontend", EMPTY)));
            return new ResourceFileToCopy(resourceFile, true, FTP);
        } else if (filename.startsWith("META-INF/dump")) {
            resourceFile = new File("%s%s%s".formatted(applicationConfig.getDump(), separator, filename.replace("META-INF/dump", EMPTY)));
            return new ResourceFileToCopy(resourceFile, true, storageType);
        } else {
            return new ResourceFileToCopy(null, false, storageType);
        }
    }

    private void deleteResourceFileIfExists(File resourceFile) throws IOException {

        if (applicationConfig.isRewrite() && resourceFile.exists() && !resourceFile.isDirectory()) {
            logger.info("Removing file %s from server root".formatted(resourceFile.getPath()));
            Files.delete(resourceFile.toPath());
        }
    }

    private boolean needToExtractFile(boolean copy, File resourceFile) {
        return resourceFile != null && (applicationConfig.isRewrite() || !resourceFile.exists()) && copy;
    }

    private String getRelativePath(File resourceFile) {
        final int startIndex = applicationConfig.getStaticPath().length() - 1;
        return resourceFile.getAbsolutePath().substring(startIndex, resourceFile.getAbsolutePath().indexOf(resourceFile.getName()));
    }

    private class CreateFilesystemDirectory implements Consumer<ResourceFileToCopy> {
        @Override
        public void accept(ResourceFileToCopy resourceFileToCopy) {
            ofNullable(resourceFileToCopy.resourceFile()).ifPresent(this::doAccept);
        }

        private void doAccept(File file) {
            logger.debug("Creating directory %s".formatted(file.getAbsolutePath()));
            if (!file.mkdir()) {
                logger.error("Failed to create directory %s".formatted(file.getAbsolutePath()));
            }
        }
    }

    private record ResourceFileToCopy(File resourceFile, boolean copy,
                                      StorageType storageType) {}
}
