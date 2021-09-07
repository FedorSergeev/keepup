package io.keepup.cms.core.service;


public class TestNotSerializableEntity {

    @ContentId
    private Long testId;

    @ContentMapping("not_serializable_wrapper")
    private NotSerializableStringWrapper value;

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public NotSerializableStringWrapper getValue() {
        return value;
    }

    public void setValue(NotSerializableStringWrapper value) {
        this.value = value;
    }
}
