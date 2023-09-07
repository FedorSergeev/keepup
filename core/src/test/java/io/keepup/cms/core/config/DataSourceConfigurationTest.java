package io.keepup.cms.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceConfigurationTest {

    private DataSourceConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new DataSourceConfiguration();
    }

    @Test
    void namingStrategy() {
        final var namingStrategy = configuration.namingStrategy();
        final var tableName = namingStrategy.getTableName(DataSourceConfiguration.class);
        assertNotNull(tableName);
    }
}