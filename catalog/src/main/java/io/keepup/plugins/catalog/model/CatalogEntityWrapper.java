package io.keepup.plugins.catalog.model;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * Wrapper for entities implementing {@link CatalogEntity} interface and their {@link Layout}
 * objects linked by {@link CatalogEntity#getLayoutName()} value.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class CatalogEntityWrapper<T extends CatalogEntity> extends CatalogEntityBaseWrapper<T> {
    private boolean success;
    private String error;

    /**
     * Check if response is successful
     *
     * @return true if response is successfull
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Set response success status
     *
     * @param success true if response is successfull
     */
    public void setSuccess(final boolean success) {
        this.success = success;
    }

    /**
     * Get error message
     *
     * @return error message
     */
    public String getError() {
        return error;
    }

    /**
     * Set error message
     *
     * @param error error message
     */
    public void setError(final String error) {
        this.error = error;
    }

    /**
     * Constructs an erroneous response wrapper with the specified error message.
     *
     * @param message error message
     * @return        wrapper object
     */
    public static Mono<CatalogEntityWrapper<CatalogEntity>> error(final String message) {
        var response = new CatalogEntityWrapper<>();
        response.setSuccess(false);
        response.setError(message);
        return  Mono.just(response);
    }

    /**
     * Constructs successful response wrapper with the specified error message.
     *
     * @param catalogEntity found entity
     * @param layout        associated layout
     * @return              wrapper object
     */
    public static Mono<CatalogEntityWrapper<CatalogEntity>> success(final CatalogEntity catalogEntity, final Mono<Layout> layout) {
        return layout.map(view -> getCatalogEntityWrapper(catalogEntity, view))
                .switchIfEmpty(Mono.just(getCatalogEntityWrapper(catalogEntity, null)));
    }

    @NotNull
    private static CatalogEntityWrapper<CatalogEntity> getCatalogEntityWrapper(CatalogEntity catalogEntity, Layout view) {
        final var response = new CatalogEntityWrapper<>();
        response.setSuccess(true);
        response.setEntity(catalogEntity);
        response.setLayout(view);
        return response;
    }
}
