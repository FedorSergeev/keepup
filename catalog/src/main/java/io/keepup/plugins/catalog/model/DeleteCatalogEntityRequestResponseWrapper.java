package io.keepup.plugins.catalog.model;

import io.keepup.cms.rest.controller.AbstractResponseWrapper;

/**
 * Wrapper for response to delete {@link CatalogEntity} by id web request
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class DeleteCatalogEntityRequestResponseWrapper extends AbstractResponseWrapper {

    public static DeleteCatalogEntityRequestResponseWrapper success() {
        var response = new DeleteCatalogEntityRequestResponseWrapper();
        response.setSuccess(true);
        return response;
    }

    public static DeleteCatalogEntityRequestResponseWrapper error(String error) {
        var response = new DeleteCatalogEntityRequestResponseWrapper();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
}
