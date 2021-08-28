package io.keepup.cms.core.plugins;

import io.keepup.cms.core.annotation.Deploy;
import io.keepup.cms.core.annotation.Plugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Comparator.comparingInt;
import static java.util.Optional.ofNullable;

/**
 * Processor for beans annotated with KeepUP annotations.
 *
 * @author Fedor Sergeev
 * @version 2.0
 * @since 1.4
 */
@Component
public class PluginServiceManager implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {

    private final Log log = LogFactory.getLog(getClass());

    private final Map<String, KeepupExtension> plugins = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean == null || beanName == null) {
            log.error("Attempt to pass emty bean or bean name to plugin service processor");
            return bean;
        }
        Class<?> beanClass = bean.getClass();
        if ((beanClass.isAnnotationPresent(Plugin.class) && bean instanceof PluginService)
         || (beanClass.isAnnotationPresent(Deploy.class) && bean instanceof BasicDeployService)) {
            log.info(String.format("Keepup managed component %s found", beanClass.getName()));
            KeepupExtension extension = (KeepupExtension)bean;
            if (plugins.putIfAbsent(ofNullable(extension.getName()).orElse("unknown"), extension) != null) {
                log.debug("Plugin with name %s already exists in Keepup configuration".formatted(extension.getName()));
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * All plugins are being deployed and initialized according to the sequence, there is no specified mechanism
     * for asynchronous plugin startup
     *
     * @param event event fired when application is ready
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.debug("Application is ready to start, beginning plugins deployment");
        plugins.entrySet()
                .stream()
                .filter(entry -> entry.getValue()
                        .getClass()
                        .isAnnotationPresent(Deploy.class))
                .map(Map.Entry::getValue)
                .filter(KeepupExtension::isEnabled)
                .map(PluginServiceManager::applyDeployService)
                .forEach(BasicDeployService::deploy);
        log.debug("Plugins deployment finished");

        log.debug("Beginning plugins initialization");
        plugins.entrySet()
                .stream()
                .filter(entry -> entry.getValue()
                        .getClass()
                        .isAnnotationPresent(Plugin.class))
                .map(Map.Entry::getValue)
                .filter(KeepupExtension::isEnabled)
                .map(PluginServiceManager::applyPluginService)
                .sorted(comparingInt(KeepupExtension::getInitOrder))
                .forEach(PluginService::init);
        log.debug("Plugins initialization finished");
    }

    private static BasicDeployService applyDeployService(KeepupExtension value) {
        return (BasicDeployService) value;
    }

    private static PluginService applyPluginService(KeepupExtension value) {
        return (PluginService) value;
    }
}
