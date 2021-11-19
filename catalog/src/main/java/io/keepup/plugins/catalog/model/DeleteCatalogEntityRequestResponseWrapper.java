package io.keepup.plugins.catalog.model;

/**
 * Wrapper for response to delete {@link CatalogEntity} by id web request
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class DeleteCatalogEntityRequestResponseWrapper {
    private boolean success;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

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
