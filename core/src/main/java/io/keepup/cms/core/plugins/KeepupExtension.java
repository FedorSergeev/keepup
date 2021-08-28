package io.keepup.cms.core.plugins;

/**
 * Basic interface for plugin entities.
 *
 * @author Fedor Sergeev
 */
public interface KeepupExtension {

    /**
     * Fetches the plugin name.
     *
     * @return plugin name
     * @since 0.6.4
     */
    String getName();

    /**
     * Specifies initialization order.
     *
     * @return order corresponding current service initialization index
     */
    int getInitOrder();

    /**
     * Sets extension initialization order. Sometimes boot order influences the
     * data.
     *
     * @param initOrder initialization order
     */
    void setInitOrder(int initOrder);

    /**
     * @return true if service is not disabled by configuration parameters
     */
    boolean isEnabled();
}
