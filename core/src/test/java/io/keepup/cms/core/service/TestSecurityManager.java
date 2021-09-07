package io.keepup.cms.core.service;

import java.lang.reflect.ReflectPermission;
import java.security.Permission;

/**
 * This security manager has more strict permissions for final and private fields modification
 */
public class TestSecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {
        if (perm instanceof ReflectPermission && "suppressAccessChecks".equals(perm.getName())) {
            for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                if ((elem.getClassName().contains("FieldUtils")) && "writeField".equals(elem.getMethodName())) {
                    throw new SecurityException();
                }
            }
        }
    }
}
