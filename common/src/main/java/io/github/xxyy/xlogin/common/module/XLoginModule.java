/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.module;

import io.github.xxyy.lib.intellij_annotations.NotNull;

/**
 * Represents a module of xLogin which can be enabled and disabled.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */
public abstract class XLoginModule {
    @NotNull
    private final String name;
    boolean enabled = false;

    protected XLoginModule() {
        this.name = getClass().getSimpleName();
    }

    @NotNull
    public String getName() {
        return name;
    }

    public abstract void enable();

    public final boolean isEnabled() {
        return enabled;
    }
}
