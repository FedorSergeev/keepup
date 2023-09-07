package io.keepup.cms.core.plugins;

import io.keepup.cms.core.JarHelper;
import io.keepup.cms.core.annotation.Deploy;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
class AbstractKeepupDeployBeanTest {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private TestAbstractKeepupDeployBeanImpl testAbstractKeepupDeployBean;
    @Autowired
    private ApplicationConfig applicationConfig;
    @Mock
    private StaticContentDeliveryService mockStaticContentDeliveryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(mockStaticContentDeliveryService.store(ArgumentMatchers.any(File.class), ArgumentMatchers.anyString()))
                .thenReturn(new TransferOperationResult().ok());
    }

    @Test
    void checkUnitFolder() {
        Long testFolderId = testAbstractKeepupDeployBean.checkUnitFolder(0L, "testFolder").block();
        Assertions.assertNotNull(testFolderId);
    }

    @Test
    void deployFromJar() throws URISyntaxException {
        var jarHelper = new JarHelper();
        jarHelper.setApplicationConfig(applicationConfig);
        jarHelper.setContentDeliveryService(mockStaticContentDeliveryService);
        List<KeepupPluginConfiguration> configurations = null;
        Throwable throwable = null;
        URLClassLoader urlClassLoader = null;
        File file = new File(getClass().getClassLoader().getResource("mock-keepup-plugin.jar").toURI());
        try (JarFile jarFile = new JarFile(file)) {

            Enumeration<JarEntry> jarEntries = jarFile.entries();
            URL[] urls = {new URL("jar:file:" + file.getAbsolutePath() + "!/")};
            urlClassLoader = URLClassLoader.newInstance(urls);

            while (jarEntries.hasMoreElements()) {
                JarEntry je = jarEntries.nextElement();
                if (je.getName().endsWith(".class")) {
                    String className = je.getName().substring(0, je.getName().length() - 6);
                    className = className.replace('/', '.');
                    Class<?> currentClass = urlClassLoader.loadClass(className);
                    log.debug("Loaded class %s".formatted(currentClass.getName()));
                    if (currentClass.isAnnotationPresent(Deploy.class)) {
                        final AbstractKeepupDeployBean bean = (AbstractKeepupDeployBean)currentClass.getConstructor(null)
                                .newInstance(null);
                        bean.setApplicationConfig(applicationConfig);
                        bean.setJarHelper(jarHelper);
                        bean.setInitOrder(25);
                        configurations = ((List)bean.getConfigurations());
                        bean.deploy();
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to process jar file: %s".formatted(ex.toString()));
            throwable = ex;
        } finally {
            if (urlClassLoader != null) {
                try {
                    urlClassLoader.close();
                } catch (IOException e) {
                    log.error("Could not close url Classloader object: %s".formatted(e.toString()));
                }
            }
        }
        Assertions.assertNull(throwable);
        Assertions.assertNotNull(configurations);
        Assertions.assertFalse(configurations.isEmpty());
        Assertions.assertNotNull(configurations.get(0).getConfigByName("testConfig"));
        Assertions.assertNull(configurations.get(0).getConfigByName("nonExistingConfig"));
    }
}