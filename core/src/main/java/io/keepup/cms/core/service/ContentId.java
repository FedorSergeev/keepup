package io.keepup.cms.core.service;

import java.lang.annotation.*;

/**
 * Marks the annotated field as primary identifier for {@link io.keepup.cms.core.persistence.Content}
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ContentId {}
