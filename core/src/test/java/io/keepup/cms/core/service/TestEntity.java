package io.keepup.cms.core.service;

import java.io.Serializable;


public class TestEntity implements Serializable {

    @ContentId
    private Long testId;

    @ContentMapping("some_value")
    private String someValue;

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }
}
