package io.keepup.cms.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.Paths.get;

/**
 * Basic Keepup application configuration
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Configuration
public class KeepupConfiguration {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Path to directory on server where static files are being stored
     */
    @Value("${keepup.paths.static:}")
    private String staticPath;

    /**
     * Defines handler function for relative paths
     *
     * @return a composed function that routes static content
     */
    @Bean
    public RouterFunction<ServerResponse> imageRouter() {
        if (staticPath.isBlank()) {
            staticPath = getDefaultStaticFolderPath();
        }
        try {
            Files.createDirectories(Paths.get(staticPath));
            log.info("Created directory for static resources storage: %s".formatted(staticPath));
        } catch (IOException e) {
            log.error("Failed to create directory %s: %s".formatted(staticPath, e.toString()));
        }
        return RouterFunctions
                    .resources("/**", new FileSystemResource(staticPath));
    }

    private String getDefaultStaticFolderPath() {
        return "%s/%s".formatted(get(".").toAbsolutePath().normalize().toString(), "static");
    }
}
