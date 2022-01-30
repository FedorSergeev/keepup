package io.keepup.plugins.catalog.service;

import io.keepup.cms.core.service.ContentId;
import io.keepup.cms.core.service.ContentMapping;
import io.keepup.plugins.catalog.model.CatalogEntity;

public class CatalogEntityTestImpl implements CatalogEntity {
    @ContentId
    private Long id;

    @ContentMapping("value")
    private String value;

    @ContentMapping("parent_id")
    private Long parentId;

    public CatalogEntityTestImpl() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getLayoutName() {
        return "testLayout";
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}