package io.keepup.plugins.catalog.model;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogEntityListWrapper<T extends CatalogEntity> {

    private List<T> entities;
    private boolean success;
    private String error;

    public CatalogEntityListWrapper() {
        entities = new ArrayList<>();
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

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public static Mono<CatalogEntityListWrapper<CatalogEntity>> error(String message) {
        var response = new CatalogEntityListWrapper<>();
        response.setSuccess(false);
        response.setError(message);
        return  Mono.just(response);
    }

    public static Mono<CatalogEntityListWrapper<CatalogEntity>> success(List<CatalogEntity> catalogEntities) {
            final var response = new CatalogEntityListWrapper<>();
            response.setSuccess(true);
            response.getEntities().addAll(catalogEntities.stream()
                    .collect(Collectors.toList()));
            return Mono.just(response);
    }
}
