package io.keepup.plugins.catalog.model;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * Wrapper for entities implementing {@link CatalogEntity} interface and their {@link Layout}
 * objects linked by {@link CatalogEntity#getLayoutName()} value.
 *
 * @author fedor Sergeev
 * @since 2.0
 */
public class CatalogEntityWrapper<T extends CatalogEntity> extends CatalogEntityBaseWrapper<T> {
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

    public static Mono<CatalogEntityWrapper<CatalogEntity>> error(String message) {
        var response = new CatalogEntityWrapper<>();
        response.setSuccess(false);
        response.setError(message);
        return  Mono.just(response);
    }

    public static Mono<CatalogEntityWrapper<CatalogEntity>> success(CatalogEntity catalogEntity, Mono<Layout> layout) {
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
