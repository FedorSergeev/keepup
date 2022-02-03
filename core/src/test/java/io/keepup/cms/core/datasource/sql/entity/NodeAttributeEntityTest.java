package io.keepup.cms.core.datasource.sql.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Spy;

class NodeAttributeEntityTest {

    NodeAttributeEntity userAttribute;

    @Spy
    ObjectMapper mapper = Mockito.spy(new ObjectMapper());

    @BeforeEach
    void setUp() throws JsonProcessingException {
        Mockito.when(mapper.writeValueAsBytes(ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("Spy exception"){});
        AbstractEntityAttribute.mapper = mapper;
    }

    @AfterEach
    void tearDown() {
        AbstractEntityAttribute.mapper = new ObjectMapper();
    }

    /**
     * Assert the default behaviour for cases with errors during serialization
     */
    @Test
    void serializeValue() {
        userAttribute = new NodeAttributeEntity(1L, "key", "serializable");
        Assertions.assertEquals(0, userAttribute.getAttributeValue().length);
        Assertions.assertEquals("java.lang.Byte[]", userAttribute.getJavaClass());
    }

}