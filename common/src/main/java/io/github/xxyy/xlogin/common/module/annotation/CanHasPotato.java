/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.module.annotation;

import io.github.xxyy.lib.intellij_annotations.NotNull;

import java.lang.annotation.*;

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
    @NotNull Class<?> value();
}
