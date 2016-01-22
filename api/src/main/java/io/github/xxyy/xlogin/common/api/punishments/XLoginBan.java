/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.api.punishments;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;

import java.sql.Timestamp;

/**
 * Represents the formal confirmation of a temporary or permanent denail of access to the network for a player.
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/09/14
 */
public interface XLoginBan extends Punishment {
    /**
     * @return the date and time when this ban expires or NULL if this ban does not expire. This may change afterwards.
     */
    @Nullable
    Timestamp getExpiryTime();

    /**
     * @return a string representation of the return value of {@link #getExpiryTime()}
     */
    @NotNull
    String getExpiryString();
}
