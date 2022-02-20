package io.keepup.cms.core.service;

import java.lang.annotation.*;

/**
 * Maps class field to {@link io.keepup.cms.core.persistence.Content} attribute
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ContentMapping {
    /**
     * Name of {@link io.keepup.cms.core.persistence.Content} attribute key.
     *
     * @return attribute key
     */
    String value() default "";
}
