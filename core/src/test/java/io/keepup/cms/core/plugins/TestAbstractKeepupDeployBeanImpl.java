package io.keepup.cms.core.plugins;

import io.keepup.cms.core.annotation.Deploy;
import io.keepup.cms.core.annotation.Plugin;
import org.springframework.stereotype.Service;

@Service
@Deploy
@Plugin
public class TestAbstractKeepupDeployBeanImpl extends AbstractKeepupDeployBean {
    public TestAbstractKeepupDeployBeanImpl() {
        super("testAbstractKeepupDeployBean");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
