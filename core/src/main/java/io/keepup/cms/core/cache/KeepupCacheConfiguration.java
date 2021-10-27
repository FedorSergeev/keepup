package io.keepup.cms.core.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

import static io.keepup.cms.core.datasource.dao.sql.SqlContentDao.CONTENT_CACHE_NAME;

/**
 * Contains configurations for different cache types.
 */
@Configuration
@EnableCaching
public class KeepupCacheConfiguration {

    @Profile("dev")
    @Bean("cacheManager")
    public CacheManager simpleCacheManager() {
        var cacheManager = new SimpleCacheManager();
        var contentCache = new ConcurrentMapCache(CONTENT_CACHE_NAME, false);
        cacheManager.setCaches(Collections.singletonList(contentCache));
        return cacheManager;
    }
}
