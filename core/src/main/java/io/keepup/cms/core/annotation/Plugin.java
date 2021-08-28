package io.keepup.cms.core.annotation;

import org.springframework.context.annotation.Condition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 * Simple marker interface to get components responsible for data deployment 
 * on core project startup. Standard KeepUP CMS annotation.
 * </p>
 * @author Fedor Sergeev
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {
    Class<? extends Condition>[] condition() default {};
}
