package io.keepup.plugins.catalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Table view and entity fields organization for Content nodes.
 *
 * @author Fedor Sergeev
 */
public class LayoutApiAttribute {
    @JsonProperty("table")
    private boolean table;
    @JsonProperty("key")
    private String key;
    @JsonProperty("name")
    private String name;
    @JsonProperty("resolve")
    private AttributeType resolve;
    @JsonProperty("tag")
    private String tag;

    public boolean isTable() {
        return table;
    }

    public void setTable(boolean table) {
        this.table = table;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getResolve() {
        return resolve;
    }

    public void setResolve(AttributeType resolve) {
        this.resolve = resolve;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LayoutApiAttribute that = (LayoutApiAttribute) o;
        return table == that.table
                && Objects.equals(key, that.key)
                && Objects.equals(name, that.name)
                && resolve == that.resolve
                && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, key, name, resolve, tag);
    }
}
