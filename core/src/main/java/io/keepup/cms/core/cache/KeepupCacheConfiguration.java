package io.keepup.cms.core.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.util.Collections;

import static io.keepup.cms.core.cache.CacheNames.CONTENT_CACHE_NAME;
import static java.util.Collections.singleton;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.cache.RedisCacheManager.builder;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

/**
 * Contains configurations for different cache types.
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@Configuration
@EnableCaching
public class KeepupCacheConfiguration {
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Redis host
     */
    @Value("${spring.redis.host:localhost}")
    private String host;

    /**
     * Redis port
     */
    @Value("${spring.redis.port:6379}")
    private int port;

    /**
     * Connection factory component that creates Lettuce -based connections
     *
     * @return thread-safe factory of reactive Redis connections
     */
    @Bean
    @Profile("redis")
    @Primary
    public LettuceConnectionFactory redisConnectionFactory() {
        log.debug("Initializing new Lettuce connection factory for Redis with host %s and port %d".formatted(host, port));
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    /**
     * Redis data code access simplifier
     *
     * @param redisConnectionFactory Redis connection factory
     * @return                       Helper component for data access
     */
    @Bean
    @Profile("redis")
    public RedisTemplate<String, Serializable> redisCacheTemplate(LettuceConnectionFactory redisConnectionFactory) {
        log.debug("Instantiating Redis cache template bean");
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * Cache manager for Redis
     *
     * @param factory Redis connection factory
     * @return        cache manager instance
     */
    @Bean
    @Primary
    @Profile("redis")
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        log.debug("Instantiating Redis cache manager bean");
        var config = defaultCacheConfig();
        var redisCacheConfiguration = config
                .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return builder(factory)
                .cacheDefaults(redisCacheConfiguration)
                .initialCacheNames(singleton(CONTENT_CACHE_NAME))
                .build();
    }

    /**
     * Simple cache manager based on {@link java.util.concurrent.ConcurrentHashMap}. Mostly is used for
     * testing purposes.
     *
     * @return cache manager
     */
    @Profile("dev")
    @Bean("cacheManager")
    public CacheManager simpleCacheManager() {
        var cacheManager = new SimpleCacheManager();
        var contentCache = new ConcurrentMapCache(CONTENT_CACHE_NAME, false);
        cacheManager.setCaches(Collections.singletonList(contentCache));
        return cacheManager;
    }
}
