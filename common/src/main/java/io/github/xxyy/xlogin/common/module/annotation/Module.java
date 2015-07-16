package io.github.xxyy.xlogin.common.module.annotation;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.xlogin.common.module.XLoginModule;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to annotate various parameters of modules
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {
    @NotNull Class<? extends XLoginModule>[] dependencies() default {};

    boolean enableByDefault() default false;
}
