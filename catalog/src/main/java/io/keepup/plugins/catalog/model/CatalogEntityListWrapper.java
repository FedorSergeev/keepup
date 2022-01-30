package io.keepup.plugins.catalog.model;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class CatalogEntityListWrapper<T extends CatalogEntity> {
    private List<T> parents;
    private List<T> entities;
    private List<Layout> layouts;
    private boolean success;
    private String error;

    public CatalogEntityListWrapper() {
        entities = new ArrayList<>();
        layouts = new ArrayList<>();
    }

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

    public List<T> getParents() {
        return parents;
    }

    public void setParents(List<T> parents) {
        this.parents = parents;
    }

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public List<Layout> getLayouts() {
        return layouts;
    }

    public void setLayouts(List<Layout> layouts) {
        this.layouts = layouts;
    }

    @Override
    public String toString() {
        return "success = %s, error = %s, parents = [%s], entities = [%s], layouts = [%s]"
                .formatted(success,
                        error,
                        getObjectListAsString(parents),
                        getObjectListAsString(entities),
                        getObjectListAsString(layouts));
    }

    public static CatalogEntityListWrapper<CatalogEntity> error(String message) {
        var response = new CatalogEntityListWrapper<>();
        response.setSuccess(false);
        response.setError(message);
        return  response;
    }

    public static Mono<CatalogEntityListWrapper<CatalogEntity>> success(List<CatalogEntity> catalogEntities) {
        final var response = new CatalogEntityListWrapper<>();
        response.setSuccess(true);
        response.getEntities().addAll(catalogEntities);
        return Mono.just(response);
    }

    public static Mono<CatalogEntityListWrapper<CatalogEntity>> success(List<CatalogEntity> catalogEntities, List<Layout> layouts) {
        return success(catalogEntities).map(wrapper -> withLayouts(layouts, wrapper));
    }

    @NotNull
    private static CatalogEntityListWrapper<CatalogEntity> withLayouts(List<Layout> layouts, CatalogEntityListWrapper<CatalogEntity> wrapper) {
        wrapper.setLayouts(layouts);
        return wrapper;
    }

    private String getObjectListAsString(List<?> items) {
        if (items == null) {
            return "null";
        }
        var stringBuilder = new StringBuilder();
        for (int index = 0; index < items.size(); index++) {
            stringBuilder.append(items.get(index).toString());
            if (index < items.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }
}
