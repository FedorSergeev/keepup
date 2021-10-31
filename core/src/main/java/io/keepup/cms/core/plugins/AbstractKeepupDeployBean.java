package io.keepup.cms.core.plugins;

import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.datasource.resources.StaticContentDeliveryService;
import io.keepup.cms.core.datasource.resources.StorageType;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static io.keepup.cms.core.datasource.resources.StorageType.FTP;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Implements basic actions for plugin data deployment. Notice that all the
 * static files are to be copied from directories within the file system.
 *
 * @author Fedor Sergeev
 */
public abstract class AbstractKeepupDeployBean implements PluginService, BasicDeployService {

    private static final String SERVER_DIRECTORY = "/META-INF/server";
    private static final String FRONTEND_DIRECTORY = "/META-INF/frontend";
    private static final String DUMP_DIRECTORY = "/META-INF/dump";

    @Autowired
    protected DataSourceFacade dataSourceFacade;

    protected ApplicationConfig applicationConfig;
    protected Log logger;
    protected String pluginName;
    protected String name;
    protected Iterable<KeepupPluginConfiguration> configurations;
    private StaticContentDeliveryService contentDeliveryService;
    protected int initOrder;


    // region lambda maps
    private final Map<StorageType, Consumer<ResourceFileToCopy>> directoryProcessors = new EnumMap<>(StorageType.class);
    // endregion

    // region public API

    @Autowired
    public void setContentDeliveryService(StaticContentDeliveryService contentDeliveryService) {
        this.contentDeliveryService = contentDeliveryService;
    }

    @Autowired
    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    /**
     * Developers can override this method in order to provide some
     * technical tasks solution in there specific fields.
     */
    @Override
    public void init() {
        logger.info("No tasks to run after basic configuration of %s plugin is completed".formatted(pluginName));
    }

    /**
     * Developers can override this method in order to provide some
     * technical tasks solution in there specific fields.
     *
     * @param args arguments
     */
    @Override
    public void init(String[] args) {
        logger.info("No tasks to run after basic configuration of %s plugin is completed".formatted(pluginName));
    }

    @Override
    public void deploy() {
        logger.info("Deploying plugin '%s' data".formatted(pluginName));
        String decodedPath = getDecodedPath();
        var dataFile = new File(decodedPath);

        if (!dataFile.isDirectory()) {
            if (!dataFile.exists() && decodedPath.endsWith(".jar")) {
                logger.debug("Attempting to load jar library form application runtime jar");
                // try to copy jar file from outer jar (application)
                try {
                    decodedPath = decodedPath.substring(decodedPath.indexOf(".jar/lib") + 4);
                    dataFile = new File("tmp");
                    logger.info("Data file for jar processing: " + dataFile.getAbsolutePath());
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("BOOT-INF" + decodedPath)), dataFile);
                } catch (IOException e) {
                    logger.error("Failed to fetch plugin from application jar: %s".formatted(e.toString()));
                }
            }
            logger.info("File: %s exists: %s".formatted(dataFile.getAbsolutePath(), Boolean.toString(dataFile.exists())));
            try (var jarDataFile = new JarFile(dataFile)) {
                logger.info("Unpacking from jar library, location: %s, setting up users dump directory %s".formatted(
                        decodedPath,
                        applicationConfig.getDump()));
                Enumeration<JarEntry> enumEntries = jarDataFile.entries();
                processJarStaticResources(jarDataFile, enumEntries);

                logger.info("Deployed successfully");
            } catch (IOException
                    | IllegalArgumentException
                    | NoSuchElementException
                    | SecurityException ex) {
                logger.error(ex.toString());
            }
        } else {
            setUpStaticContentFromDirectory(decodedPath);
        }
    }

    public Mono<Long> checkUnitFolder(long parentId, String folderName) {
        if (dataSourceFacade != null) {
            return dataSourceFacade.getContentByParentIdAndAttributeValue(parentId, "meta", folderName)
                    .collect(Collectors.toList())
                    .flatMap(nodes -> {
                        if (nodes.isEmpty()) {
                            logger.info("Setting basic node with ''meta'' = %s".formatted(folderName));
                            Content newNode = new Node();
                            newNode.setParentId(parentId);
                            newNode.setOwnerId(0L);
                            newNode.setDefaultPrivileges();
                            newNode.setAttribute("meta", folderName);
                            return dataSourceFacade.createContent(newNode);
                        } else {
                            return Mono.just(nodes.get(0).getId());
                        }
                    });
        } else {
            logger.error("Could not initialize plugin %s because DAO component is null".formatted(pluginName));
            return Mono.empty();
        }
    }


    @Override
    public int getInitOrder() {
        return initOrder;
    }

    @Override
    public void setInitOrder(int initOrder) {
        this.initOrder = initOrder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Iterable<KeepupPluginConfiguration> getConfigurations() {
        return configurations;
    }

    // endregion

    /**
     * Constructor with bean name definition. Currently the name is used only
     * in in logs during the system start, in deploy and init methods.
     *
     * @param pluginName name of the plugin
     */
    protected AbstractKeepupDeployBean(String pluginName) {
        this();
        this.pluginName = pluginName;
    }

    /**
     * Default bean constructor.
     */
    protected AbstractKeepupDeployBean() {
        logger = LogFactory.getLog(getClass().getName());
        initOrder = 10000;
        configurations = new ArrayList<>();
        directoryProcessors.put(StorageType.FILESYSTEM, new CreateFilesystemDirectory());
    }

    private String getDecodedPath() {
        String decodedPath = null;
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        // need to check if application runs inside of jar or in the filesystem
        if (new File(path).isDirectory()) {
            logger.debug("Path %s is a directory".formatted(path));
            return path;
        }
        try {
            decodedPath = URLDecoder.decode(path, UTF_8.toString());
            decodedPath = decodedPath.replace("!/BOOT-INF/classes!", EMPTY);
            decodedPath = decodedPath.replace("!/BOOT-INF", EMPTY);
            if (decodedPath.endsWith(".class")) {
                decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("classes") + 7);
            }
            if (decodedPath.endsWith("!/")) {
                decodedPath = decodedPath.substring(0, decodedPath.length() - 2);
            }
        } catch (UnsupportedEncodingException ex) {
            logger.error("Failed to check out decoded path for plugin %s reason: %s".formatted(name, ex.toString()));
        }
        return ofNullable(decodedPath).orElse(EMPTY);
    }

    private void processJarStaticResources(JarFile jar, Enumeration<JarEntry> enumEntries) throws IOException {
        while (enumEntries.hasMoreElements()) {

            var file = enumEntries.nextElement();
            if (file.getName().startsWith("META-INF/")) {
                ResourceFileToCopy copyCondition = getCopyCondition(file);
                var copy = copyCondition.isCopy();
                var resourceFile = copyCondition.getResourceFile();
                if (needToExtractFile(copy, resourceFile)) {

                    if (file.isDirectory()) {
                        ofNullable(directoryProcessors.get(copyCondition.getStorageType()))
                                .ifPresent(processor -> processor.accept(copyCondition));
                        continue;
                    }
                    copyFileToSystem(jar, resourceFile, file, copyCondition.getStorageType());
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
            if (FTP.equals(storageType)) {
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
        if (FTP.equals(storageType) && resourceFile.exists()) {
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
        if (file.getName().startsWith(serverFiles)) {

            if (file.getName().startsWith("META-INF/server/")) {
                resourceFile = new File("%s/resources%s%s".formatted(applicationConfig.getDocumentRoot(), separator, file.getName().replace(serverFiles, EMPTY)));
                deleteResourceFileIfExists(resourceFile);
                return new ResourceFileToCopy(resourceFile, true, storageType);
            } else {
                return new ResourceFileToCopy(null, false, storageType);
            }
        } else if (file.getName().startsWith("META-INF/frontend")) {
            resourceFile = new File("%s%s%s".formatted(applicationConfig.getStaticPath(), separator, file.getName().replace("META-INF/frontend", EMPTY)));
            return new ResourceFileToCopy(resourceFile, true, FTP);
        } else if (file.getName().startsWith("META-INF/dump")) {
            resourceFile = new File("%s%s%s".formatted(applicationConfig.getDump(), separator, file.getName().replace("META-INF/dump", EMPTY)));
            return new ResourceFileToCopy(resourceFile, true, storageType);
        } else {
            return new ResourceFileToCopy(null, false, storageType);
        }
    }

    private void deleteResourceFileIfExists(File resourceFile) throws IOException {

        if (applicationConfig.isRewrite() && resourceFile.exists() && !resourceFile.isDirectory()) {
            logger.info(format("Removing file %s from server root", resourceFile.getPath()));
            Files.delete(resourceFile.toPath());
        }
    }

    private boolean needToExtractFile(boolean copy, File resourceFile) {
        return resourceFile != null && (applicationConfig.isRewrite() || !resourceFile.exists()) && copy;
    }

    /**
     * Copies static resources from directory, usually is called from tests and not from production.
     *
     * @param decodedPath path to directory containing the needed data
     */
    private void setUpStaticContentFromDirectory(String decodedPath) {
        try {
            if (new File(decodedPath.concat(SERVER_DIRECTORY)).exists()) {
                copyDirectory(new File(decodedPath.concat(SERVER_DIRECTORY)), new File(applicationConfig.getDocumentRoot().concat("/resources")));
            }
            if (new File(decodedPath.concat(FRONTEND_DIRECTORY)).exists()) {
                copyDirectory(new File(decodedPath.concat(FRONTEND_DIRECTORY)), new File(applicationConfig.getStaticPath()));
            }
            if (new File(decodedPath.concat(DUMP_DIRECTORY)).exists()) {
                copyDirectory(new File(decodedPath.concat(DUMP_DIRECTORY)), new File(applicationConfig.getDump()));
            }
        } catch (IOException e) {
            logger.error("Failed to copy static resources from directory: %s".formatted(e.toString()));
        }
    }

    private class CreateFilesystemDirectory implements Consumer<ResourceFileToCopy> {
        @Override
        public void accept(ResourceFileToCopy resourceFileToCopy) {
            ofNullable(resourceFileToCopy.getResourceFile()).ifPresent(this::doAccept);
        }

        private void doAccept(File file) {
            logger.debug("Creating directory %s".formatted(file.getAbsolutePath()));
            if (!file.mkdir()) {
                logger.error("Failed to create directory %s".formatted(file.getAbsolutePath()));
            }
        }
    }

    private static class ResourceFileToCopy {
        private final File resourceFile;
        private final StorageType storageType;
        private final boolean copy;

        public ResourceFileToCopy(File resourceFile, boolean copy, StorageType storageType) {
            this.resourceFile = resourceFile;
            this.copy = copy;
            this.storageType = storageType;
        }

        public File getResourceFile() {
            return resourceFile;
        }

        public boolean isCopy() {
            return copy;
        }

        public StorageType getStorageType() {
            return storageType;
        }
    }

    private String getRelativePath(File resourceFile) {
        final int startIndex = applicationConfig.getStaticPath().length() - 1;
        return resourceFile.getAbsolutePath().substring(startIndex, resourceFile.getAbsolutePath().indexOf(resourceFile.getName()));
    }


}
