/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.common.module.annotation;

import li.l1t.xlogin.common.module.XLoginModule;

import javax.annotation.Nonnull;
import java.lang.annotation.*;

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
    @Nonnull Class<? extends XLoginModule>[] dependencies() default {};

    boolean enableByDefault() default false;
}