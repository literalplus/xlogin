package io.github.xxyy.xlogin.common.module.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a module or plugin..um..potato instance into a module field.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CanHasPotato {
    /**
     * @return what kind of potato to can has or NULL to inject plugin potato
     */
    Class<?> value();
}
