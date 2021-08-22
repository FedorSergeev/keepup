package io.keepup.cms.core.boot;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Bean created specially to break the application startup process
 */
@Service
@Profile("local-testing")
public class ThrowableBean {
    public ThrowableBean() {
        throw new RuntimeException("application startup process killed");
    }
}
