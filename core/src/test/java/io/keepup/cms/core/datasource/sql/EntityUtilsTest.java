package io.keepup.cms.core.datasource.sql;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests to exclude nullability and other weird things
 */
class EntityUtilsTest {

    private final EntityUtilsTest self = this;

    @Test
    void toByteArray() {
        assertNotNull(EntityUtils.toByteArray(null));
        assertNotNull(EntityUtils.toByteArray(1));
        assertNotNull(EntityUtils.toByteArray(new ClassThatCauseMappingException()));
    }


    private static class ClassThatCauseMappingException implements Serializable {
        private final ClassThatCauseMappingException self = this;

        @Override
        public String toString() {
            return self.getClass().getName();
        }
    }
}