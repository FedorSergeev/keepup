package io.keepup.cms.core.cache;

import com.github.fppt.jedismock.RedisServer;
import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.H2ConsoleService;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static io.keepup.cms.core.cache.CacheNames.CONTENT_CACHE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * These tests are similar to {@link RedisKeepupCacheConfigurationTest}, but use {@link RedisServer} from
 * jedis-mock project, so they can run on machines with Apple Silicon
 */
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
        "spring.redis.port=60380",
        "spring.redis.host=localhost"
})
@DataR2dbcTest
class RedisCacheMockTest {
    private RedisServer server;

    @Autowired
    private RedisCacheManager cacheManager;
    @Autowired
    private DataSourceFacade dataSourceFacade;

    @BeforeEach
    void setUp() throws Exception {
        server = RedisServer.newRedisServer(60380);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void getRecentlyAddedValueFromRedisCache() {
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
