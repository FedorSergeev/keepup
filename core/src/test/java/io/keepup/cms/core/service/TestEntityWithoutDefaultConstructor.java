package io.keepup.cms.core.service;

import java.io.Serializable;


public class TestEntityWithoutDefaultConstructor implements Serializable {

    @ContentId
    private Long testId;

    public TestEntityWithoutDefaultConstructor(Long testId) {
        this.testId = testId;
    }
}
