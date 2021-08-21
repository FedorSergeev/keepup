package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.datasource.sql.entity.UserAttributeEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "h2"})
@ContextConfiguration(classes = {
        KeepupApplication.class,
        KeepupCacheConfiguration.class,
        CacheAdapter.class,
        WebFluxConfig.class,
        ReactiveNodeEntityRepository.class,
        ReactiveNodeAttributeEntityRepository.class,
        ReactiveUserEntityRepository.class,
        ReactiveRoleByUserEntityRepository.class,
        ReactiveUserAttributeEntityRepository.class,
        DataSourceConfiguration.class,
        R2dbcConfiguration.class,
        SqlContentDao.class,
        SqlFileDao.class,
        SqlUserDao.class,
        DataSourceFacadeImpl.class})
class ReactiveUserAttributeEntityRepositoryTest {

    private  static final long USER_ID = 112L;

    @Autowired
    private ReactiveUserAttributeEntityRepository userAttributeEntityRepository;


    @Test
    void findAllByUserId() {
        final String VALUE = UUID.randomUUID().toString();
        UserAttributeEntity userAttributeEntity = new UserAttributeEntity();
        userAttributeEntity.setUserId(USER_ID);
        userAttributeEntity.serializeValue("key",VALUE);

        List<UserAttributeEntity> userAttributes = userAttributeEntityRepository.save(userAttributeEntity)
                .thenMany(userAttributeEntityRepository.findAllByUserId(USER_ID))
                .collect(Collectors.toList())
                .block();

        UserAttributeEntity userAttributeEntity_1 = new UserAttributeEntity(USER_ID, "1", VALUE);


        // region assert
        assertThrows(IllegalArgumentException.class, () -> new UserAttributeEntity(USER_ID, null, VALUE));
        assertThrows(IllegalArgumentException.class, () -> userAttributeEntity_1.serializeValue(null, VALUE));
                assertNotNull(userAttributes);
        assertFalse(userAttributes.isEmpty());
        assertEquals(1, userAttributes.size());
        assertEquals(userAttributeEntity.getUserId(), userAttributes.get(0).getUserId());
        assertEquals(userAttributeEntity.getAttributeKey(), userAttributes.get(0).getAttributeKey());
        assertEquals(new String(userAttributeEntity.getAttributeValue()), new String(userAttributes.get(0).getAttributeValue()));
        assertEquals(userAttributeEntity.getCreationTime(), userAttributes.get(0).getCreationTime());
        assertEquals(userAttributeEntity.getModificationTime(), userAttributes.get(0).getModificationTime());
        // endregion
    }

    @Test
    void deleteByUserId() {
        UserAttributeEntity userAttributeEntity = new UserAttributeEntity();
        userAttributeEntity.setUserId(USER_ID);
        userAttributeEntity.serializeValue("key", null);
        List<UserAttributeEntity> userAttributeEntities = userAttributeEntityRepository.save(userAttributeEntity)
                .thenMany(userAttributeEntityRepository.deleteByUserId(USER_ID))
                .thenMany(userAttributeEntityRepository.findAllByUserId(USER_ID))
                .collect(Collectors.toList())
                .block();

        // region assert
        assertNotNull(userAttributeEntities);
        assertTrue(userAttributeEntities.isEmpty());
        // endregion
    }

    @Test
    void findAllByUserIdWithAttributeNames() {
        UserAttributeEntity userAttributeEntity_0 = new UserAttributeEntity();
        userAttributeEntity_0.setUserId(USER_ID);
        userAttributeEntity_0.serializeValue("key_0", "firstValue");

        UserAttributeEntity userAttributeEntity_1 = new UserAttributeEntity();
        userAttributeEntity_1.setUserId(USER_ID);
        userAttributeEntity_1.serializeValue("key_1", "secondValue");

        UserAttributeEntity userAttributeEntity_2 = new UserAttributeEntity();
        userAttributeEntity_2.setUserId(USER_ID + 1);
        userAttributeEntity_2.serializeValue("key_0", "thirdValue");

        List<NodeAttributeEntity> firstUserAttributes = userAttributeEntityRepository.saveAll(Arrays.asList(userAttributeEntity_0, userAttributeEntity_1, userAttributeEntity_2))
                .collect(Collectors.toList())
                .thenMany(userAttributeEntityRepository.findAllByUserIdWithAttributeNames(USER_ID, Collections.singletonList("key_0")))
                .collect(Collectors.toList())
                .block();

        List<NodeAttributeEntity> secondUserAttributes = userAttributeEntityRepository.findAllByUserIdWithAttributeNames(USER_ID + 1, Collections.singletonList("key_1"))
                .collect(Collectors.toList())
                .block();

        // region assert
        assertNotNull(firstUserAttributes);
        assertNotNull(secondUserAttributes);
        assertTrue(secondUserAttributes.isEmpty());
        assertEquals(2, firstUserAttributes.size());
        assertEquals("\"firstValue\"", new String(firstUserAttributes.get(0).getAttributeValue()));

        // endregion
    }
}