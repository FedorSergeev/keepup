package io.keepup.cms.core.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple check for base methods
 */
class NodeTest {

    Node node;

    @BeforeEach
    void setUp() {
        node = new Node();
    }

    @Test
    void testToString() {
        assertNotNull(node.toString());
    }

    @Test
    void testEqualsHashCode() {
        node.setId(1L);
        node.setAttributes(new HashMap<>());
        assertTrue(node.getAttributes().isEmpty());
        node.setAttribute("testKey", "testValue");

        var node2 = new Node();
        node2.setId(2L);
        node.setAttributes(new HashMap<>());
        node.setAttribute("testKey", "testValue");

        assertNotEquals(node2, node);

        Map<Node, Integer> map = new HashMap<>();
        map.put(node, 1);
        map.put(node2, 2);

        assertEquals(2, map.size());
        assertEquals(1, map.get(node));
        assertEquals(2, map.get(node2));
    }

    @Test
    void removeAttribute() {
        node.addAttribute("toRemove", true);
        node.removeAttribute("toRemove");

        assertFalse(node.hasAttribute("toDelete"));
    }
}