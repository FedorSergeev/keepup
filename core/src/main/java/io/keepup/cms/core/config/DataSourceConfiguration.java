package io.keepup.cms.core.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.persistence.Table;
import javax.sql.DataSource;

/**
 * Contains the set of beans for data source management
 *
 * @since 2.0.0
 * @author Fedor Sergeev
 */
@Configuration(proxyBeanMethods = false)
public class DataSourceConfiguration {

    @Value("${spring.datasource.driver-class-name:org.h2.Driver}")
    private String driverClassName;
    @Value("${spring.datasource.jdbc-url:jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL}")
    private String url;
    @Value("${spring.datasource.username:sa}")
    private String login;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.liquibase.change-log}")
    private String changeLog;
    @Value("${spring.liquibase.enabled:true}")
    private boolean liquibaseEnabled;

    /**
     * Liquibase configuration
     *
     * @return component responsible for data source consistency check and update
     */
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        var liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(liquibaseEnabled);
        return liquibase;
    }

    /**
     * Provides {@link DataSource} object for liquibase
     *
     * @return configured data source
     */
    @Bean("dataSource")
    public DataSource dataSource() {
        var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(login);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * Naming strategy for synchronization between annotated class and database tables
     *
     * @return Naming strategy that looks up for {@link Table} annotation to get the table name
     */
    @Bean
    public NamingStrategy namingStrategy() {
        return new NamingStrategy() {
            @Override
            public String getTableName(Class<?> type) {
                if (type.isAnnotationPresent(Table.class)) {
                    return type.getAnnotation(Table.class).name();
                }
                return NamingStrategy.super.getTableName(type);
            }
        };
    }
}
