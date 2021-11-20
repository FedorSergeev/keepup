package io.keepup.cms.core.cache;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

import static io.keepup.cms.core.cache.CacheNames.CONTENT_CACHE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@ActiveProfiles({"h2", "redis"})
@ContextConfiguration(classes = {
        KeepupApplication.class,
        KeepupCacheConfiguration.class,
        CacheAdapter.class,
        WebFluxConfig.class,
        ReactiveNodeEntityRepository.class,
        ReactiveNodeAttributeEntityRepository.class,
        ReactiveUserEntityRepository.class,
        DataSourceConfiguration.class,
        R2dbcConfiguration.class,
        SqlContentDao.class,
        SqlFileDao.class,
        SqlUserDao.class,
        DataSourceFacadeImpl.class,
        LettuceConnectionFactory.class
})
@TestPropertySource(properties = {
        "spring.redis.port=60379",
        "spring.redis.database=0",
        "spring.redis.host=localhost"
})
@DataR2dbcTest
class RedisKeepupCacheConfigurationTest {

    @Value("${spring.redis.port}")
    int redisPort;

    @Autowired
    private RedisCacheManager cacheManager;
    @Autowired
    private DataSourceFacade dataSourceFacade;

    private RedisServer redisServer;

    @BeforeEach
    void setUp() {
        redisServer = new RedisServer(redisPort);
        redisServer.start();
    }

    @AfterEach
    void tearDown() {
        redisServer.stop();
    }

    /**
     * Warning! For now this tests only works on MacOS x86 and *nix-based systems as there is a number of
     * unsolved problems connected to Redis embedded distribution. Please comment this tests or mark
     * it with @{@link org.junit.Ignore} annotation if you use MacOS on Apple Silicon or Windows until we fix it.
     */
    @Test
    void redisCacheManager() {
        var node = new Node();
        node.setParentId(0L);
        node.setDefaultPrivileges();
        node.setOwnerId(0L);
        var content = dataSourceFacade.createContent(node)
                .flatMap(id -> dataSourceFacade.getContent(id)).block();

        var valueWrapper = cacheManager.getCache(CONTENT_CACHE_NAME)
                .get(content.getId());
        assertNotNull(valueWrapper);
        assertNotNull(valueWrapper.get());
        assertEquals(content.getId(), ((Content)valueWrapper.get()).getId());
        assertEquals(content.getAttributes(), ((Content)valueWrapper.get()).getAttributes());
    }
}