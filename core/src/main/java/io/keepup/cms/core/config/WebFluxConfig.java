package io.keepup.cms.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Reactive web application configuration
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * Jackson object mapper referenced from application context
     * @return application object mapper
     */
    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
