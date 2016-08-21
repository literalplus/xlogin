/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.common.api.punishments;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

/**
 * Represents an interface to a ban system which allows to permanently and temporarily
 * ban players from the server.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/09/14
 */
public interface BanManager {

    /**
     * Gets or fetches a ban information for a specified target UUID. This will not fetch from database every time it's
     * called, but instead cache objects between calls. After some time, data will be updated from database.
     *
     * @param uuid the UUID to look for
     * @return a ban information for specified criteria or NULL if no such ban exists
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    XLoginBan getBanInfo(UUID uuid);

    /**
     * This gets a ban information directly from database, ignoring any cached data that may be present.
     *
     * @param uuid the UUID to look for
     * @return a ban information for specified criteria which is guaranteed to be present in the database at call-time
     * or NULL if no such ban exists.
     * @see #getBanInfo(java.util.UUID)
     */
    @Nullable
    XLoginBan forceGetBanInfo(UUID uuid);

    /**
     * Checks whether a given UUID is target of any currently valid bans. This is using the same cache as {@link #getBanInfo(java.util.UUID)}.
     * @param uuid the unique id to match
     * @return whether a not yet expired ban info is present in cache or database
     */
    boolean isBanned(UUID uuid);

    /**
     * Creates a new ban. This updates mutable fields if there's already a ban for that target UUID.
     * @param targetId the unique id of the target player
     * @param sourceId the unique id of the source player or {@link li.l1t.common.XycConstants#NIL_UUID}
     * @param sourceServerName the name of the server that ban originated from or NULL
     * @param reason the human-readable reason for the ban. Should not be empty or vague by convention.
     * @param expiryTime the date and time of this ban's expiry or NULL to create a permanent ban
     * @return the created ban information
     */
    @Nonnull
    XLoginBan setBanned(@Nonnull UUID targetId, @Nonnull UUID sourceId, @Nullable String sourceServerName, @Nonnull String reason, @Nullable Date expiryTime);
}
