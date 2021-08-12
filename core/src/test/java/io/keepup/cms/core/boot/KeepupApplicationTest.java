package io.keepup.cms.core.boot;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;

class KeepupApplicationTest {

    /**
     * The purpose of the test is to start the application with all beans in future
     */
    @Test
    void main() {
        Assert.assertThrows(UnsatisfiedDependencyException.class, KeepupApplication::main);
    }
}