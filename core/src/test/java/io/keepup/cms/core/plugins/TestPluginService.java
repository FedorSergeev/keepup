package io.keepup.cms.core.plugins;

import io.keepup.cms.core.annotation.Plugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Plugin
public class TestPluginService implements PluginService {
    private final Log log = LogFactory.getLog(getClass());

    private int initOrder;
    private String name;

    public TestPluginService() {
        name = "testPlugin";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getInitOrder() {
        return initOrder;
    }

    @Override
    public void setInitOrder(int initOrder) {
        this.initOrder = initOrder;
    }

    @Override
    public void init() {
        log.info("Test plugin " + getName() + " initialized successfully");
    }

    @Override
    public void init(String[] args) {
        init();
    }

    @Override
    public Iterable<KeepupPluginConfiguration> getConfigurations() {
        return new ArrayList<>();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }
}
