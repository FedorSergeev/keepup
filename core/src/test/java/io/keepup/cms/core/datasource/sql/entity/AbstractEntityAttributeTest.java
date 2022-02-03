package io.keepup.cms.core.datasource.sql.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractEntityAttributeTest {

    AbstractEntityAttributeImpl abstractEntityAttribute;

    /**
     * Assert no exception will be thrown and we just get an empty byte array
     */
    @Test
    void toByteArray() {
        abstractEntityAttribute = new AbstractEntityAttributeImpl();
        assertEquals(0, abstractEntityAttribute.toByteArray(new NotSerializableType()).length);
    }

    static class AbstractEntityAttributeImpl extends AbstractEntityAttribute {}

    static class NotSerializableType {}
}