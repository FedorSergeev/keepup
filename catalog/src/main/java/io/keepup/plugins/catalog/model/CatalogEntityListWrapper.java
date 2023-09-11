package io.keepup.plugins.catalog.model;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper object for {@link CatalogEntity}. Used by MVC controller and handled additional information about success
 * status, possible error message and list of layout templates attached to the specified catalog entities list.
 *
 * @param <T> entity type
 */
public class CatalogEntityListWrapper<T extends CatalogEntity> {
    private List<T> parents;
    private List<T> entities;
    private List<Layout> layouts;
    private boolean success;
    private String error;

    /**
     * Default constructor, instantiates inner collections
     */
    public CatalogEntityListWrapper() {
        entities = new ArrayList<>();
        layouts = new ArrayList<>();
    }

    /**
     * Check if catalog list request was successful
     *
     * @return true if all the entities and layouts have been fetched successfully and false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Set true if catalog list request was successful
     *
     * @param success true if all the entities and layouts have been fetched successfully and false otherwise
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
     * Get the list of catalog entity parents (each of entities has an identifier equals to other entity's parent identifier)
     *
     * @return requested entity parent records
     */
    public List<T> getParents() {
        return parents;
    }

    /**
     * Set the list of catalog entity parents (each of entities has an identifier equals to other entity's parent identifier)
     *
     * @param parents requested entity parent records
     */
    public void setParents(final List<T> parents) {
        this.parents = parents;
    }

    /**
     * Get the list of entities fetched according to request
     *
     * @return list of found entities
     */
    public List<T> getEntities() {
        return entities;
    }

    /**
     * Set the list of entities fetched according to request
     *
     * @param entities list of found entities
     */
    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    /**
     * Set the list of layout templates associated with the resulting list of entities
     *
     * @return list of layout templates
     */
    public List<Layout> getLayouts() {
        return layouts;
    }

    /**
     * Set the list of layout templates associated with the resulting list of entities
     *
     * @param layouts list of layout templates
     */
    public void setLayouts(final List<Layout> layouts) {
        this.layouts = layouts;
    }

    /**
     * toString method overriding
     *
     * @return object as a string
     */
    @Override
    public String toString() {
        return "success = %s, error = %s, parents = [%s], entities = [%s], layouts = [%s]"
                .formatted(success,
                        error,
                        getObjectListAsString(parents),
                        getObjectListAsString(entities),
                        getObjectListAsString(layouts));
    }

    /**
     * Constructs the response wrapper object with erroneous status
     *
     * @param message error message describing, what went wrong
     * @return        new wrapper object
     */
    public static CatalogEntityListWrapper<CatalogEntity> error(final String message) {
        var response = new CatalogEntityListWrapper<>();
        response.setSuccess(false);
        response.setError(message);
        return  response;
    }

    /**
     * Constructs the response wrapper object with successful status
     *
     * @param catalogEntities list of entities
     * @return                new wrapper object
     */
    public static Mono<CatalogEntityListWrapper<CatalogEntity>> success(final List<CatalogEntity> catalogEntities) {
        final var response = new CatalogEntityListWrapper<>();
        response.setSuccess(true);
        response.getEntities().addAll(catalogEntities);
        return Mono.just(response);
    }

    /**
     * Constructs the response wrapper object with successful status
     *
     * @param catalogEntities list of found entities
     * @param layouts         list of associated layouts
     * @return                new wrapper object
     */
    public static Mono<CatalogEntityListWrapper<CatalogEntity>> success(final List<CatalogEntity> catalogEntities,
                                                                        final List<Layout> layouts) {
        return success(catalogEntities).map(wrapper -> withLayouts(layouts, wrapper));
    }

    @NotNull
    private static CatalogEntityListWrapper<CatalogEntity> withLayouts(final List<Layout> layouts,
                                                                       final CatalogEntityListWrapper<CatalogEntity> wrapper) {
        wrapper.setLayouts(layouts);
        return wrapper;
    }

    private String getObjectListAsString(final List<?> items) {
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
