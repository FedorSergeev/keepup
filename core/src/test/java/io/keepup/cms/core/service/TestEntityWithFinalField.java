package io.keepup.cms.core.service;

import java.io.Serializable;


public class TestEntityWithFinalField implements Serializable {

    @ContentId
    private Long testId;

    @ContentMapping("some_value")
    private final String someValue = "finalValue";

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getSomeValue() {
        return someValue;
    }
}
