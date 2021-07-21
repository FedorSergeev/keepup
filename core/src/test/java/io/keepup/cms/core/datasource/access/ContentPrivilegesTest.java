package io.keepup.cms.core.datasource.access;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Just to be sure that overridden standard methods work properly
 */
class ContentPrivilegesTest {
    ContentPrivileges privileges;

    @BeforeEach
    void setUp() {
        privileges = new ContentPrivileges();
    }

    @Test
    void testToString() {
        Assertions.assertNotNull(privileges.toString());
    }

    @Test
    void testEqualsHashCode() {
        var newContentPrivileges = new ContentPrivileges();
        Map<ContentPrivileges, Integer> map = new HashMap<>();
        map.put(privileges, 1);
        map.put(newContentPrivileges, 2);
        Assertions.assertEquals(1, map.size());

    }
}