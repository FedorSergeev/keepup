package io.keepup.cms.core.plugins;

import java.io.Serializable;

/**
 * Interface to be implemented in {@link PluginService} objects
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public interface KeepupPluginConfiguration extends Serializable {

    Serializable getConfigByName(String configName);
}
