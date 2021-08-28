package io.keepup.cms.core.plugins;

/**
 * Basic customization deploy configuration.
 *
 * @author Fedor Sergeev
 */

public interface BasicDeployService extends KeepupExtension {

    /**
     * By default this method is responsible for unpacking static content from jar libraries or folders in the file system
     */
    void deploy();
}
