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
 */
@Service
public class ApplicationConfig {

    private final Log log = LogFactory.getLog(getClass());

    @Value("${keepup.cms.resources.storage_type:0}")
    private int storageType;
    @Value("${keepup.cms.resources.ftp.user:}")
    private String username;
    @Value("${keepup.cms.resources.ftp.password:}")
    private String password;
    @Value("${keepup.cms.resources.ftp.server:}")
    private String server;
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

    public int getStorageType() {
        return storageType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public String getDump() {
        return dump;
    }

    public String getDocumentRoot() {
        return documentRoot;
    }

    public String getAppData() {
        return appData;
    }

    public String getStaticPath() {
        return staticPath;
    }

    public boolean isRewrite() {
        return rewrite;
    }

    @PostConstruct
    private void processValues() {
        if (dump.isBlank()) {
            dump = "%s/%s".formatted(Paths.get(".").toAbsolutePath().normalize().toString(), "dump");
            try {
                Files.createDirectories(Paths.get(staticPath));
            } catch (IOException e) {
                log.error("Failed to create static path directory %s: %s".formatted(staticPath, e.toString()));
            }
        }
        if (staticPath.isBlank()) {
            staticPath = "%s/%s".formatted(get(".").toAbsolutePath().normalize().toString(), "static");
            try {
                Files.createDirectories(Paths.get(staticPath));
            } catch (IOException e) {
                log.error("Failed to create static path directory %s: %s".formatted(staticPath, e.toString()));
            }
        }

        if (documentRoot.isBlank()) {
            documentRoot = get(".").toAbsolutePath().normalize().toString();
        }

    }
}
