package io.keepup.cms.core.plugins;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.junit.jupiter.api.Assertions.*;

class PluginServiceManagerTest {

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;
    @Mock
    private PluginService mockPluginService;
    @Mock
    private BasicDeployService mockDeployService;

    private PluginServiceManager pluginServiceManager;
    private TestPluginService testPluginService;
    private TestKeepupDeployBean testKeepupDeployBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testPluginService = new TestPluginService();
        testKeepupDeployBean = new TestKeepupDeployBean("TestKeepupDeploy");
        pluginServiceManager = new PluginServiceManager();
    }

    @Test
    void postProcessBeforeInitialization() {
        var obj = new Object();
        assertNull(pluginServiceManager.postProcessBeforeInitialization(null, "nullName"));
        assertNotNull(pluginServiceManager.postProcessBeforeInitialization(obj, null));
        assertNotNull(pluginServiceManager.postProcessBeforeInitialization(mockPluginService, "mockPlugin"));
        assertNotNull(pluginServiceManager.postProcessBeforeInitialization(testPluginService, "mockPlugin"));
        assertNotNull(pluginServiceManager.postProcessBeforeInitialization(testPluginService, "mockPlugin"));
        assertNotNull(pluginServiceManager.postProcessBeforeInitialization(mockDeployService, "mockDeployService"));
        assertEquals(obj, pluginServiceManager.postProcessBeforeInitialization(obj, "someName"));

    }

    @Test
    void postProcessAfterInitialization() {
        var obj = new Object();
        assertNotNull(pluginServiceManager.postProcessAfterInitialization(obj, null));
        assertNull(pluginServiceManager.postProcessAfterInitialization(null, null));
    }

    @Test
    void onApplicationEvent() {
        TestPluginService testServiceWithLowerPriority = new TestPluginService();
        testServiceWithLowerPriority.setInitOrder(Integer.MAX_VALUE);
        testServiceWithLowerPriority.setName("lowerPriorityPlugin");
        pluginServiceManager.postProcessBeforeInitialization(testPluginService, "mockPlugin");
        pluginServiceManager.postProcessBeforeInitialization(testServiceWithLowerPriority, "mockPlugin1");
        pluginServiceManager.postProcessBeforeInitialization(testKeepupDeployBean, "testKeepupDeploy");
        assertDoesNotThrow(() -> pluginServiceManager.onApplicationEvent(applicationReadyEvent));
    }
}