package io.keepup.cms.core.datasource.access;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentPrivilegesFactoryTest {
    Constructor<ContentPrivilegesFactory> declaredConstructor;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        declaredConstructor = ContentPrivilegesFactory.class.getDeclaredConstructor();
        declaredConstructor.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        declaredConstructor.setAccessible(false);
    }

    @Test
    void noConstructorTest() {
        assertThrows(IllegalAccessException.class, () -> ContentPrivilegesFactory.class.getDeclaredConstructor().newInstance());
        assertThrows(InvocationTargetException.class, () -> declaredConstructor.newInstance());
    }
}