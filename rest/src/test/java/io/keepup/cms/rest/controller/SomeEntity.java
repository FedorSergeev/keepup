package io.keepup.cms.rest.controller;

import io.keepup.cms.core.service.ContentId;
import io.keepup.cms.core.service.ContentMapping;

import java.io.Serializable;

public class SomeEntity implements Serializable {
    @ContentId
    private Long id;
    @ContentMapping("value")
    private String value;

    public Long getId() {
        return id;
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
}
