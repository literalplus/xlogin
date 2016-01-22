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
import io.github.xxyy.xlogin.common.api.XLoginRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a generic punishment.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 27.8.14
 */
public interface Punishment {
    /**
     * @return the UUID of the target of this punishment
     */
    @NotNull
    UUID getTargetId();

    /**
     * @return the UUID which caused this punishment or
     * {@link io.github.xxyy.common.XycConstants#NIL_UUID} if no player can be
     * assigned to this punishment
     */
    @NotNull
    UUID getSourceId();

    /**
     * Fetches the name of this punishment's target from a given repository.
     * @param repo the repository to use to get the requested name
     * @return the name from given repository or a string of the form "unknown/[UUID]"
     * if the given repository does not contain matching data
     */
    @NotNull
    String getTargetName(XLoginRepository repo);

    /**
     * Fetches the name of this punishment's source from a given repository.
     * @param repo the repository to use to get the requested name
     * @return the name from given repository or a string of the form "unknown/[UUID]"
     * if the given repository does not contain matching data
     */
    @NotNull
    String getSourceName(XLoginRepository repo);

    /**
     * @return the date and time when this punishment was issued
     */
    @NotNull
    Timestamp getTimestamp();

    /**
     * @return the user-defined reason for this punishment.
     */
    @NotNull
    String getReason();

    /**
     * @return the BungeeCord name of the server this punishment was issued from. The name might have changed since and this may be NULL.
     */
    @Nullable
    String getSourceServerName();

    /**
     * @return whether this punishment is still valid, that means that it still exists in database.
     */
    boolean isValid();

    /**
     * Saves this punishment.
     * @return this object, for convenience.
     */
    @NotNull
    Punishment save();

    /**
     * Deletes this punishment from the database. This also marks this object as invalid.
     */
    void delete();

    /**
     * @return the {@link java.util.Date} corresponding to the timestamp returned by {@link #getTimestamp()}.
     */
    @NotNull
    Date getDate();
}
