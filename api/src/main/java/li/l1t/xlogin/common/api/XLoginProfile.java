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


import li.l1t.common.lib.com.mojang.api.profiles.Profile;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Represents a xLogin player profile.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public interface XLoginProfile extends Profile {
    /**
     * @return the last recorded name of this player.
     */
    @Nonnull
    String getName();

    /**
     * @return Unique ID of the player. Mojang UUID if {@link #isPremium()} returns TRUE,
     * otherwise name-based offline UUID.
     */
    @Nonnull
    UUID getUniqueId();

    /**
     * @return whether this player has bought Minecraft from Mojang and enabled premium authentication.
     */
    boolean isPremium();

    /**
     * @return the last known IP of this profile.
     */
    String getLastIp();
}
