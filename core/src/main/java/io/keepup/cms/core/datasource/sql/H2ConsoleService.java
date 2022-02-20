package io.keepup.cms.core.datasource.sql;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * We start the separate server for testing purposes at another port as default configured
 * H2 console cannot run with Netty
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Component
@Profile("h2")
public class H2ConsoleService {
    @Value("${spring.h2.server.port.web:8082}")
    private String webPort;
    @Value("${spring.h2.server.port.tcp:8084}")
    private String tcpPort;

    private Server webServer;

    private Server tcpServer;

    /**
     * Start H2 database TCP server and web console.
     *
     * @throws java.sql.SQLException if the server could not be started
     */
    @EventListener(ContextRefreshedEvent.class)
    public void start() throws java.sql.SQLException {
        this.webServer = org.h2.tools.Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();
        this.tcpServer = org.h2.tools.Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();
    }

    /**
     * Stops H2 TCP server and web console when application context closes.
     */
    @EventListener(ContextClosedEvent.class)
    public void stop() {
        this.tcpServer.stop();
        this.webServer.stop();
    }
}
