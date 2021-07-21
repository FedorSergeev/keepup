package io.keepup.cms.core.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Launcher class
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class KeepupApplication {

    public static void main(String... args) {
        SpringApplication.run(KeepupApplication.class);
    }
}
