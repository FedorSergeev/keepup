package io.keepup.cms.core.boot;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.UndeclaredThrowableException;

@RunWith(SpringRunner.class)
class KeepupApplicationTest {

    /**
     * The purpose of the test is to start the application with all beans in future
     */
    @Test
    void main() {
        Assert.assertThrows(UndeclaredThrowableException.class, KeepupApplication::main);
    }
}