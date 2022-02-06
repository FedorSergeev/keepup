package io.keepup.cms.core.config;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@Profile("h2")
@EnableR2dbcRepositories
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    @Value("${spring.datasource.url:r2dbc:h2:mem:default;DB_CLOSE_DELAY=-1;SCHEMA=KEEPUP;}")
    private String url;
    @Value("${spring.datasource.username:sa}")
    private String username;

    @Bean
    public @NotNull H2ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        .url(url)
                        .username(username)
                        .build()
        );
    }
}
