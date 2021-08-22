package io.keepup.cms.core.plugins;

/**
 * This interface must be implemented by every KeepUP CMS plugin
 *
 * @author Fedor Sergeev
 */
public interface PluginService extends KeepupExtension {

    /**
     * Standard service initialize method. 
     */
    void init();
    
    /**
     * Service initialize with array of arguments.
     *
     * @param args arguments for the service
     */
    void init(String [] args);

    /**
     * Returns a list of special configurations.
     *
     * @return plugin configurations
     * @since 0.6.5
     */
    Iterable<KeepupPluginConfiguration> getConfigurations();
}
