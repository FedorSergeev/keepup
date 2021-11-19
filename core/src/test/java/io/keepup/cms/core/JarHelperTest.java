package io.keepup.cms.core;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.KeepupConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.resources.FtpFileProcessor;
import io.keepup.cms.core.datasource.resources.StaticContentDeliveryService;
import io.keepup.cms.core.datasource.resources.TransferOperationResult;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.plugins.PluginServiceManager;
import io.keepup.cms.core.plugins.TestAbstractKeepupDeployBeanImpl;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.keepup.cms.core.JarHelper.JAR_BOOT_INF_LIB;

@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "h2"})
@TestPropertySource(properties = {
        "keepup.plugins.rewrite=true",
        "keepup.cms.resources.storage_type=0"
})
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
        KeepupConfiguration.class,
        PluginServiceManager.class,
        ApplicationConfig.class,
        TestAbstractKeepupDeployBeanImpl.class,
        StaticContentDeliveryService.class,
        FtpFileProcessor.class,
        JarHelper.class
})
@DataR2dbcTest
class JarHelperTest {
    @Autowired
    JarHelper jarHelper;

    @Mock
    private StaticContentDeliveryService mockStaticContentDeliveryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(mockStaticContentDeliveryService.store(ArgumentMatchers.any(File.class), ArgumentMatchers.anyString()))
                .thenReturn(new TransferOperationResult().ok());
    }

    /**
     * Attempts to deploy plugin stored in the application as Jar file inside of application Jar
     * @throws URISyntaxException
     */
    @Test
    void processPluginFromApplicationLibEntry() throws URISyntaxException, MalformedURLException {
        var applicationJarFile = new File(getClass().getClassLoader().getResource("mock-keepup-app.jar").toURI());
        try (JarFile jarFile = new JarFile(applicationJarFile)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();
                if (entry.getName().contains("BOOT-INF/lib/mock-plugin-2.0-SNAPSHOT.jar")) {
                    jarHelper.processPluginFromApplicationLibEntry(entry.getName(),
                                                                   jarFile, "testInnerMockPlugin");
                }
            }
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }
}