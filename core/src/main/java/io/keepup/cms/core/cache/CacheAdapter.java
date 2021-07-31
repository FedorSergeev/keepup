package io.keepup.cms.core.cache;

import io.keepup.cms.core.datasource.dao.SqlDataSource;
import io.keepup.cms.core.persistence.Content;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.ofNullable;

/**
 * Adapter component for different cache types
 */
@Service("cacheAdapter")
public class CacheAdapter {

    private final Log log = LogFactory.getLog(getClass());
    @Autowired
    private CacheManager cacheManager;

    /**
     * Looks up for the {@link Content} record in cache
     *
     * @param  id record identifier
     * @return record with the specified identifier or empty Optional in case if nothing was found in
     * cache, or if id is null
     */
    public Optional<Content> getContent(final Long id) {
        if (id == null) {
            log.error("Content identifier is null");
            return Optional.empty();
        }
        return Optional.ofNullable(cacheManager.getCache(SqlDataSource.CONTENT_CACHE_NAME))
                .map(cache -> cache.get(id, Content.class));
    }

    /**
     * Puts the newer Content object version into the cache if there hashes are not equal or
     * if the older version is not present
     *
     * @param content record to be updated
     * @return currently persisted record or null if
     */
    public Content updateContent(Content content) {
        AtomicReference<Content> success = new AtomicReference<>(content);
        ofNullable(cacheManager.getCache(SqlDataSource.CONTENT_CACHE_NAME))
                .ifPresent(cache -> {
                    var cachedContent = cache.get(content.getId(), Content.class);
                    if (cachedContent == null || cachedContent.hashCode() != content.hashCode()) {
                        cache.put(content.getId(), content);
                    } else {
                        success.set(cachedContent);
                    }
                });
        return success.get();
    }

    /**
     * Evicts the cache if {@link Content} record was there
     * @param id record identifier
     */
    public void deleteContent(Long id) {
        if (id == null) {
            log.error("Cannot delete Content record from cache with empty id");
            return;
        }
        ofNullable(cacheManager.getCache(SqlDataSource.CONTENT_CACHE_NAME))
                .ifPresent(cache -> cache.evictIfPresent(id));
    }
}
