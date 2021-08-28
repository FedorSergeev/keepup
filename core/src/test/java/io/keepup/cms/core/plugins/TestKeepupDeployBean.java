package io.keepup.cms.core.plugins;

import io.keepup.cms.core.annotation.Deploy;

@Deploy
public class TestKeepupDeployBean extends AbstractKeepupDeployBean {
    public TestKeepupDeployBean(String pluginName) {
        super(pluginName);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
