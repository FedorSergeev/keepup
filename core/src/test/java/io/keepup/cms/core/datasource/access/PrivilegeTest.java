package io.keepup.cms.core.datasource.access;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PrivilegeTest {

    Privilege privilege = new Privilege();

    @Test
    void testToString() {
        assertNotNull(privilege.toString());
        assertEquals("read = false, write = false, execute = false, children = false", privilege.toString());
    }

    @Test
    void testEqualsHashCode() {
        Privilege anotherPrivilege = new Privilege();
        Map<Privilege, Integer> map = new HashMap<>();
        map.put(privilege, 1);
        map.put(anotherPrivilege, 2);
        assertEquals(1, map.size());
    }
}