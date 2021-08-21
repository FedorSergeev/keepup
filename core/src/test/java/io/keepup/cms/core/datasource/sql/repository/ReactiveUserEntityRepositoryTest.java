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
import io.keepup.cms.core.datasource.sql.entity.RoleByUserIdEntity;
import io.keepup.cms.core.datasource.sql.entity.RoleEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
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
        DataSourceConfiguration.class,
        R2dbcConfiguration.class,
        SqlContentDao.class,
        SqlFileDao.class,
        SqlUserDao.class,
        DataSourceFacadeImpl.class})
class ReactiveUserEntityRepositoryTest {

    @Autowired
    private ReactiveRoleByUserEntityRepository roleByUserEntityRepository;

    @Test
    void findAllByRoles() {
        String admin = "admin_role";
        String developer = "developer_role";
        RoleByUserIdEntity roleUserEntity_0 = new RoleByUserIdEntity();
        roleUserEntity_0.setRole(admin);
        roleUserEntity_0.setUserId(3L);
        RoleByUserIdEntity roleUserEntity_1 = new RoleByUserIdEntity();
        roleUserEntity_1.setRole(developer);
        roleUserEntity_1.setUserId(3L);
        RoleByUserIdEntity roleUserEntity_2 = new RoleByUserIdEntity();
        roleUserEntity_2.setRole(developer);
        roleUserEntity_2.setUserId(4L);

        roleByUserEntityRepository.deleteAll().block();

        roleByUserEntityRepository.saveAll(Arrays.asList(roleUserEntity_0, roleUserEntity_1, roleUserEntity_2))
                .collect(Collectors.toList()).block();

        List<RoleByUserIdEntity> developers = roleByUserEntityRepository.findAllWhoHasRoles(singletonList(developer))
                .collect(Collectors.toList())
                .block();

        List<RoleByUserIdEntity> admins = roleByUserEntityRepository.findAllWhoHasRoles(singletonList(admin))
                .collect(Collectors.toList())
                .block();

        List<RoleByUserIdEntity> nobody = roleByUserEntityRepository.findAllWhoHasRoles(singletonList("nobody"))
                .collect(Collectors.toList())
                .block();

        List<RoleByUserIdEntity> adminsAndDevelopers = roleByUserEntityRepository.findAllWhoHasRoles(Arrays.asList(developer, admin))
                .collect(Collectors.toList())
                .block();

        assertNotNull(developers);
        assertNotNull(admins);
        assertNotNull(nobody);
        assertNotNull(adminsAndDevelopers);
        assertEquals(3, developers.size());
        assertEquals(2, admins.size());
        assertTrue(nobody.isEmpty());
    }

    @Test
    void updateRole() {
        long roleId = 1786L;
        RoleByUserIdEntity roleUserEntity_0 = new RoleByUserIdEntity();
        roleUserEntity_0.setId(roleId);
        roleUserEntity_0.setRole("admin");
        roleUserEntity_0.setUserId(51L);

        RoleByUserIdEntity roleUserEntity_1 = new RoleByUserIdEntity();
        roleUserEntity_1.setRole("admin");
        roleUserEntity_1.setUserId(52L);

        RoleByUserIdEntity roleUserEntity_2 = new RoleByUserIdEntity();
        roleUserEntity_2.setRole("developer");
        roleUserEntity_2.setUserId(52L);

        roleByUserEntityRepository.updateRole(roleUserEntity_0.getUserId(), "admin").block();
        roleByUserEntityRepository.updateRole(roleUserEntity_1.getUserId(), "admin").block();
        roleByUserEntityRepository.updateRole(roleUserEntity_1.getUserId(), "developer").block();

        List<RoleByUserIdEntity> user0RoleEntities = roleByUserEntityRepository.findAllByUserIds(singletonList(roleUserEntity_0.getUserId()))
                .collect(Collectors.toList()).block();

        List<String> roles = roleByUserEntityRepository.findRolesByUserId(roleUserEntity_0.getUserId())
                .map(RoleEntity::getRole)
                .collect(Collectors.toList())
                .block();

        List<RoleByUserIdEntity> user1RoleEntities = roleByUserEntityRepository.findAllByUserIds(singletonList(roleUserEntity_1.getUserId()))
                .collect(Collectors.toList()).block();

        List<String> user1RoleNames = roleByUserEntityRepository.findRolesByUserId(roleUserEntity_1.getUserId()).map(RoleEntity::getRole).collect(Collectors.toList()).block();
        roleUserEntity_0.setId(user0RoleEntities.get(0).getId());

        // region assert
        assertNotNull(user0RoleEntities);

        assertEquals(1, user0RoleEntities.size());
        assertEquals("admin", user0RoleEntities.get(0).getRole());

        assertNotNull(roles);
        assertEquals(1, roles.size());

        assertNotNull(user1RoleEntities);
        assertEquals(2, user1RoleEntities.size());

        assertNotNull(user1RoleNames);
        assertEquals(2, user1RoleNames.size());
        // endregion
    }

}