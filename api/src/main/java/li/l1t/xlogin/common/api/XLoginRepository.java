/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.l1t.xlogin.common.api;


import li.l1t.common.shared.uuid.UUIDRepository;

import javax.annotation.Nonnull;
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
    boolean isPlayerKnown(@Nonnull UUID uuid);

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
    @Nonnull
    List<? extends XLoginProfile> getProfilesByName(@Nonnull String name);

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
    @Nonnull
    List<? extends XLoginProfile> getProfiles(@Nonnull String input);

    /**
     * Fetches the local xLogin profile for a given UUID from the local xLogin database. This will never query external
     * services, such as the Mojang Profiles API.
     *
     * @param uuid the unique id of the profile to get
     * @return the retrieved profile, or null if there is no profile for that unique id
     */
    XLoginProfile getProfile(@Nonnull UUID uuid);

    /**
     * Forces the profile with given unique id to be fetched from database to the local xLogin repository cache,
     * removing any previous entries. This is especially helpful if you know that the profile has changed.
     * @param uuid the unique id of the profile to refresh
     */
    void refreshProfile(UUID uuid);
}
