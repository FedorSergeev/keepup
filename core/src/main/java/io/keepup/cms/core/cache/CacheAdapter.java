package io.keepup.cms.core.cache;

import io.keepup.cms.core.persistence.Content;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
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
public record CacheAdapter(@NonNull CacheManager cacheManager) {

    private static final Log LOG = LogFactory.getLog(CacheAdapter.class);

    /**
     * Looks up for the {@link Content} record in cache
     *
     * @param contentId record identifier
     * @return record with the specified identifier or empty Optional
     */
    public Optional<Content> getContent(final Long contentId) {
        return contentId == null
                ? logEmptyIdWarning()
                : ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .map(cache -> cache.get(contentId, Content.class));
    }

    /**
     * Puts the newer Content object version into the cache
     * if their hashes are not equal or if the older version is not present
     *
     * @param content record to be updated
     * @return currently persisted record or null
     */
    public Content updateContent(final Content content) {
        final var success = new AtomicReference<>(content);
        ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .ifPresent(cache -> {
                    final var cachedContent = cache.get(content.getId(), Content.class);
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
     * @param contentId record identifier
     */
    public void deleteContent(final Long contentId) {
        if (contentId == null) {
            LOG.error("Cannot delete Content record from cache with empty id");
            return;
        }
        ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .ifPresent(cache -> cache.evictIfPresent(contentId));
    }

    /**
     * Updated just one record attribute in case when {@link Content} entity is cached
     *
     * @param contentId      record id
     * @param attributeKey   attribute key to be updated
     * @param attributeValue new attribute value
     */
    public void updateContent(final Long contentId,
                              final String attributeKey,
                              final Serializable attributeValue) {
        ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .flatMap(cache -> ofNullable(cache.get(contentId, Content.class)))
                .ifPresent(rec -> {
                    rec.setAttribute(attributeKey, attributeValue);
                    ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                            .ifPresent(contentCache -> {
                                contentCache.put(contentId, rec);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("[CONTENT#%d] Updated attribute key = %s, value = %s".formatted(contentId, attributeKey, attributeValue));
                                }
                            });
                });
    }

    @NonNull
    private Optional<Content> logEmptyIdWarning() {
        LOG.error("Content identifier is null");
        return Optional.empty();
    }
}
