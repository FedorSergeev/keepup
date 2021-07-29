package io.keepup.cms.core.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class KeepupCacheConfigurationTest {

    /**
     * Tests Ignite casche manager instantiation
     */
    @Test
    void igniteCacheManager() {
        var keepupCacheConfiguration = new KeepupCacheConfiguration();
        var cacheManager = keepupCacheConfiguration.igniteCacheManager();
        assertNotNull(cacheManager);
    }
}