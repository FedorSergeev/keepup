package io.keepup.cms.core.cache;

import io.keepup.cms.core.persistence.Content;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static io.keepup.cms.core.cache.CacheNames.CONTENT_CACHE_NAME;
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
     * @param id record identifier
     * @return record with the specified identifier or empty Optional in case if nothing was found in
     * cache, or if id is null
     */
    public Optional<Content> getContent(final Long id) {
        if (id == null) {
            log.error("Content identifier is null");
            return Optional.empty();
        }
        return Optional.ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
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
        ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
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
     *
     * @param id record identifier
     */
    public void deleteContent(Long id) {
        if (id == null) {
            log.error("Cannot delete Content record from cache with empty id");
            return;
        }
        ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .ifPresent(cache -> cache.evictIfPresent(id));
    }

    /**
     * Updated just one record attribute in case when {@link Content} entity is cached
     *
     * @param contentId      record id
     * @param attributeKey   attribute key to be updated
     * @param attributeValue new attribute value
     */
    public void updateContent(Long contentId, String attributeKey, Serializable attributeValue) {
        ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .ifPresent(cache ->
                    ofNullable(cache.get(contentId, Content.class))
                            .ifPresent(rec -> {
                                rec.setAttribute(attributeKey, attributeValue);
                                ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                                        .ifPresent(contentCache -> {
                                            contentCache.put(contentId, rec);
                                            log.debug("[CONTENT#%d] Updated attribute key = %s, value = %s".formatted(contentId, attributeKey, attributeValue));
                                        });
                            }));
    }
}
