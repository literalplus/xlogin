/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.api;


import io.github.xxyy.common.shared.uuid.UUIDRepository;
import io.github.xxyy.lib.intellij_annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Specifies a repository for xLogin profiles which acts as an interface between xLogin API consumers and the xLogin
 * database. Implementations may choose to implement caching.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public interface XLoginRepository extends UUIDRepository {
    /**
     * Checks whether a player specified by a given UUID is known to the database.
     * Will make a query, so make sure to execute this async wherever possible.
     *
     * @param uuid the unique id of the player to find
     * @return whether that UUID is mapped to a player in the database.
     */
    boolean isPlayerKnown(@NotNull UUID uuid);

    /**
     * Fetches profiles that match the given user name from the local xLogin database. This will never query external
     * services, such as the Mojang Profiles API. If a player with Minecraft premium account is found, that one is
     * returned instantly. If not, players without Minecraft premium accounts will be returned. The result may be
     * cached by the local xLogin repository cache.
     *
     * @param name the name of the player to get, case-insensitive
     * @return a list of known profiles for that criteria
     * @see #getProfiles(String) also accepts strings formatted like UUIDs
     */
    @NotNull
    List<? extends XLoginProfile> getProfilesByName(@NotNull String name);

    /**
     * Fetches profiles that match the given criteria string from the local xLogin database. This will never query
     * external services, such as the Mojang Profiles API. If a player with Minecraft premium account is found, that
     * one is returned instantly. If not, players without Minecraft premium accounts will be returned. The result may
     * be cached by the local xLogin repository cache.
     * <p>
     *     This method handles names as well as strings formatted like a UUID.
     * </p>
     *
     * @param input the criteria string to match against
     * @return a list of known profiles for that criteria string
     * @see #getProfilesByName(String) checks for names only
     */
    @NotNull
    List<? extends XLoginProfile> getProfiles(@NotNull String input);

    /**
     * Fetches the local xLogin profile for a given UUID from the local xLogin database. This will never query external
     * services, such as the Mojang Profiles API.
     *
     * @param uuid the unique id of the profile to get
     * @return the retrieved profile, or null if there is no profile for that unique id
     */
    XLoginProfile getProfile(@NotNull UUID uuid);

    /**
     * Forces the profile with given unique id to be fetched from database to the local xLogin repository cache,
     * removing any previous entries. This is especially helpful if you know that the profile has changed.
     * @param uuid the unique id of the profile to refresh
     */
    void refreshProfile(UUID uuid);
}
