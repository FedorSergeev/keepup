package io.keepup.cms.core.plugins;

import io.keepup.cms.core.JarHelper;
import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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

    private JarHelper jarHelper;

    @Autowired
    protected DataSourceFacade dataSourceFacade;

    protected ApplicationConfig applicationConfig;
    protected Log logger;
    protected String name;
    protected Iterable<KeepupPluginConfiguration> configurations;
    protected int initOrder;

    // region public API

    @Autowired
    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Autowired
    public void setJarHelper(JarHelper jarHelper) {
        this.jarHelper = jarHelper;
    }

    /**
     * Developers can override this method in order to provide some
     * technical tasks solution in there specific fields.
     */
    @Override
    public void init() {
        logger.info("No tasks to run after basic configuration of %s plugin is completed".formatted(name));
    }

    /**
     * Developers can override this method in order to provide some
     * technical tasks solution in there specific fields.
     *
     * @param args arguments
     */
    @Override
    public void init(String[] args) {
        logger.info("No tasks to run after basic configuration of %s plugin is completed".formatted(name));
    }

    @Override
    public void deploy() {
        logger.info("Deploying plugin '%s' data".formatted(name));
        String decodedPath = getDecodedPath();
        if (decodedPath.contains(JarHelper.JAR_BOOT_INF_LIB)) {
            logger.info("Unpacking from jar library stored inside of application binary: %s".formatted(decodedPath));
            var applicationJarName = decodedPath.substring(0, decodedPath.indexOf(JarHelper.JAR_BOOT_INF_LIB) + 3);
            try (var jarDataFile = new JarFile(applicationJarName)) {
                jarHelper.processPluginFromApplicationLibEntry(decodedPath, jarDataFile, name);
            } catch (IOException e) {
                logger.error("Failed to unpack JAR content: %s".formatted(e.toString()));
            }
        }
        final var dataFile = new File(decodedPath);
        if (!dataFile.isDirectory() && dataFile.exists()) {
            try (var jarDataFile = new JarFile(dataFile)) {
                logger.info("Unpacking from jar library, location: %s, setting up users dump directory %s".formatted(
                        decodedPath,
                        applicationConfig.getDump()));
                jarHelper.processJarStaticResources(jarDataFile);
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
            logger.error("Could not initialize plugin %s because DAO component is null".formatted(name));
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
    public Iterable<KeepupPluginConfiguration> getConfigurations() {
        return configurations;
    }

    /**
     * Get the name of KeepUP plugin
     *
     * @return plugin name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Check if the plugin is enabled for current application. By default, all plugins are
     * disabled, this can prevent a number of vulnerabilities
     *
     * @return true if plugin is enabled
     */
    @Override
    public boolean isEnabled() {
        return false;
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
        name = pluginName;
    }

    /**
     * Default bean constructor.
     */
    protected AbstractKeepupDeployBean() {
        logger = LogFactory.getLog(getClass().getName());
        initOrder = 10000;
        configurations = new ArrayList<>();
    }

    private String getDecodedPath() {
        String decodedPath = null;
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            decodedPath = URLDecoder.decode(path, UTF_8.toString());
            decodedPath = decodedPath.replace("file:", EMPTY);
            decodedPath = decodedPath.replace("!/BOOT-INF/classes!", EMPTY);
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
}
