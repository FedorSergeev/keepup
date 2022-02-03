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

class UserAttributeEntityTest {

    UserAttributeEntity userAttribute;

    @Spy
    ObjectMapper mapper = Mockito.spy(new ObjectMapper());

    @BeforeEach
    void setUp() throws JsonProcessingException {
        Mockito.when(mapper.writeValueAsBytes(ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("Spy exception"){});
        AbstractEntityAttribute.mapper = mapper;
        userAttribute = new UserAttributeEntity();

    }
    @AfterEach
    void tearDown() {
        AbstractEntityAttribute.mapper = new ObjectMapper();
    }

    @Test
    void serializeValue() {
        userAttribute.serializeValue("key", new NotSerializableType());
        Assertions.assertEquals(0, userAttribute.getAttributeValue().length);
    }

    static class NotSerializableType {}
}