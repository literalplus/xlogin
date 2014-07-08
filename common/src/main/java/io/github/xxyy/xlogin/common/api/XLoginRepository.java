package io.github.xxyy.xlogin.common.api;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public interface XLoginRepository {
    /**
     * Checks whether a player specified by a given UUID is known to the database.
     * Will make a query, so make sure to execute this async wherever possible.
     *
     * @param uuid Unique Id of the player to find
     * @return Whether that UUID is mapped to a player in the database.
     */
    boolean isPlayerKnown(@NotNull UUID uuid);

    /**
     * Gets profiles that match the given user name. Premium players are returned first.
     *
     * @param name Name of the player to get. Casing is ignored.
     * @return List of known profiles for that criteria.
     *
     * @see #getProfiles(String)
     */
    @NotNull
    List<? extends XLoginProfile> getProfilesByName(@NotNull String name);

    /**
     * Gets profiles that match the given criteria. Premium players are returned first.
     * If you are certain that the input is not a UUID, use {@link #getProfilesByName(String)}
     *
     * @param input {Name of the player to get. Casing is ignored.} or {a valid UUID String}
     * @return List of known profiles for that criteria.
     */
    @NotNull
    List<? extends XLoginProfile> getProfiles(@NotNull String input);

    /**
     * Gets the profile for the given UUID.
     *
     * @param uuid UUID of the profile to get
     * @return Profile info for requested UUID or NULL if there's no such profile.
     */
    XLoginProfile getProfile(@NotNull UUID uuid);

    void refreshProfile(UUID uuid);
}
