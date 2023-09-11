package io.keepup.cms.core.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.Paths.get;

/**
 * Stores and processes properties
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Service
public class ApplicationConfig {

    private static final String SLASH = "/";
    private final Log log = LogFactory.getLog(getClass());

    @Value("${keepup.cms.resources.storage_type:0}")
    private int storageType;
    @Value("${keepup.cms.resources.ftp.user:}")
    private String ftpUsername;
    @Value("${keepup.cms.resources.ftp.password:}")
    private String ftpPassword;
    @Value("${keepup.cms.resources.ftp.server:}")
    private String ftpServer;
    @Value("${keepup.cms.resources.ftp.port:0}")
    private int port;
    @Value("${keepup.paths.dump:}")
    private String dump;
    @Value("${keepup.paths.document-root:}")
    private String documentRoot;
    @Value("${keepup.paths.app-data:}")
    private String appData;
    @Value("${keepup.paths.static:}")
    private String staticPath;
    @Value("${keepup.plugins.rewrite:false}")
    private boolean rewrite;

    /**
     * Get the storage type set for the application static content.
     *
     * @return storage type as value of {@link io.keepup.cms.core.datasource.resources.StorageType}
     * @see io.keepup.cms.core.datasource.resources.StorageType
     */
    public int getStorageType() {
        return storageType;
    }

    /**
     * Get FTP login for user to access remote host for static files storage.
     *
     * @return FTP login for user to access remote host for static files storage
     */
    public String getFtpUsername() {
        return ftpUsername;
    }

    /**
     * Get password for FTP server
     *
     * @return FTP server password
     */
    public String getFtpPassword() {
        return ftpPassword;
    }

    /**
     * Get FTP server host
     *
     * @return FTP server host
     */
    public String getFtpServer() {
        return ftpServer;
    }

    /**
     * Get FTP server port
     *
     * @return FTP server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get path to dump directory
     *
     * @return path to dump directory
     */
    public String getDump() {
        return dump.endsWith(SLASH) ? dump : dump + SLASH;
    }

    /**
     * Get path to application document root. This folder id used by the web context to serve static files.
     *
     * @return path to application document root as String
     */
    public String getDocumentRoot() {
        return documentRoot;
    }

    /**
     * Get path to folder where static content should be placed. Used by to serve static files which should
     * not be accessible from web context.
     *
     * @return path to folder where static content is stored as String
     */
    public String getStaticPath() {
        return staticPath;
    }

    /**
     * Check if static content files are to be replaced by newer versions when they are extracted from Jar
     * resources.
     *
     * @return true if static content is to be overwritten when a new copy of the file with the same name
     *         and path is being extracted from inner Jar file.
     */
    public boolean isRewrite() {
        return rewrite;
    }

    /**
     * Checks if dump and static file directories exist and attempts to create them if not
     */
    @PostConstruct
    private void processValues() {
        dump = tryCreateDirectory(dump, "dump");
        staticPath = tryCreateDirectory(staticPath, "static path");

        if (documentRoot.isBlank()) {
            documentRoot = get(".").toAbsolutePath().normalize().toString();
        }

    }

    private String tryCreateDirectory(String path, String directoryType) {
        if (path.isBlank()) {
            path = "%s/%s".formatted(Paths.get(".").toAbsolutePath().normalize().toString(), "dump");
        }
        try {
            Files.createDirectories(Paths.get(path));
            log.debug("Directory '%s' created: %s".formatted(directoryType, path));
        } catch (IOException e) {
            log.error("Failed to create %s directory %s: %s".formatted(directoryType, path, e.toString()));
        }

        return "%s/".formatted(path);
    }
}
