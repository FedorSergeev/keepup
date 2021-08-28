package io.keepup.cms.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Simple marker interface to get components responsible for data deployment 
 * on core project startup.
 *
 * @author Fedor Sergeev
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Deploy {}
