package io.keepup.plugins.catalog.model;

import io.keepup.cms.rest.controller.AbstractResponseWrapper;

/**
 * Wrapper for response to delete {@link CatalogEntity} by id web request
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class DeleteCatalogEntityRequestResponseWrapper extends AbstractResponseWrapper {

    /**
     * Creates new response wrapper success object
     *
     * @return response wrapper
     */
    public static DeleteCatalogEntityRequestResponseWrapper success() {
        final var response = new DeleteCatalogEntityRequestResponseWrapper();
        response.setSuccess(true);
        return response;
    }

    /**
     * Creates new response wrapper erroneous object
     *
     * @param error error message
     * @return      response wrapper
     */
    public static DeleteCatalogEntityRequestResponseWrapper error(final String error) {
        final var response = new DeleteCatalogEntityRequestResponseWrapper();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
}
