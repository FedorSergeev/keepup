package io.keepup.cms.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.netty.resources.LoopResources;

/**
 * Reactive web application configuration
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Limit on the number of bytes that can be buffered whenever the input stream needs to be aggregated
     */
    @Value("${keepup.web.codecs.max-memory-size:8192}")
    private int maxInMemorySize;

    /**
     * Number of concurrent threads to run inside of the web server
     */
    @Value("${keepup.server.resources.worker_count:16}")
    private int workerCount;

    /**
     * Jackson object mapper referenced from application context
     * @return application object mapper
     */
    @Bean
    @Primary
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    /**
     * Customizes Netty server
     *
     * @return factory for creating the web server
     */
    @Bean
    public ReactiveWebServerFactory reactiveWebServerFactory() {
        log.debug("Setting number of worker threads to %d".formatted(workerCount));
        var factory = new NettyReactiveWebServerFactory();

        var resourceFactory = new ReactorResourceFactory();
        resourceFactory.setLoopResources(LoopResources.create("http", workerCount, true));
        factory.setResourceFactory(resourceFactory);

        return factory;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(maxInMemorySize);
        log.debug("HTTP message codecs max in-memory size set to %s".formatted(maxInMemorySize));
    }
}
