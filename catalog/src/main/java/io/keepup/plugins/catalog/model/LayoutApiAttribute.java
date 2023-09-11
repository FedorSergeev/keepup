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

    /**
     * Check if attribute is to be rendered in table view
     *
     * @return true is attribute should be displayed in table view
     */
    public boolean isTable() {
        return table;
    }

    /**
     * Set attribute to be rendered in table view
     *
     * @param table true is attribute should be displayed in table view
     */
    public void setTable(final boolean table) {
        this.table = table;
    }

    /**
     * Get attribute key, name of entity attribute to be associated with current layout attribute
     *
     * @return attribute key
     */
    public String getKey() {
        return key;
    }

    /**
     * Set attribute key, name of entity attribute to be associated with current layout attribute
     *
     * @param key attribute key
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * Get attribute name. Can be used as table row header.
     *
     * @return attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Set attribute name. Can be used as table row header.
     *
     * @param name attribute name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get attribute resolution type. This method shows the way associated entity attribute will be displayed.
     * For instance, it can be displayed as plain text or radio buttom, image or custom HTML string.
     *
     * @return attribute resolution type
     */
    public AttributeType getResolve() {
        return resolve;
    }

    /**
     * Set attribute resolution type. This method shows the way associated entity attribute will be displayed.
     * For instance, it can be displayed as plain text or radio buttom, image or custom HTML string.
     *
     * @param resolve attribute resolution type
     */
    public void setResolve(final AttributeType resolve) {
        this.resolve = resolve;
    }

    /**
     * Get attribute HTML tag to be displayed in
     *
     * @return HTML tag as string
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set attribute HTML tag to be displayed in
     *
     * @param tag HTML tag as string
     */
    public void setTag(final String tag) {
        this.tag = tag;
    }

    /**
     * Redefinition of standard method.
     *
     * @param o abject to compare
     * @return  true if objects are equal and false otherwise
     */
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

    /**
     * Redefinition of standard method.
     *
     * @return object hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(table, key, name, resolve, tag);
    }
}
