package io.keepup.plugins.catalog.model;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

class CatalogEntityListWrapperTest {

    private static final String TEST_ERROR = "test error";

    @Test
    void error() {
        var wrapper = CatalogEntityListWrapper.error(TEST_ERROR).block();
        assertFalse(wrapper.isSuccess());
        assertEquals(TEST_ERROR, wrapper.getError());
    }

    @Test
    void success() {
        var wrapper = CatalogEntityListWrapper.success(emptyList(), emptyList()).block();
        assertTrue(wrapper.isSuccess());
        assertEquals(emptyList(), wrapper.getEntities());
        assertEquals(emptyList(), wrapper.getLayouts());
    }
}