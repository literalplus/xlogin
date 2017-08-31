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

import java.util.UUID;

/**
 * Registers authentication status of  {@link XLoginProfile}s.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public interface XLoginRegistry {
    /**
     * Checks whether the given UUID represents an authenticated player.
     *
     * @param uuid UUID to check for
     * @return whether the player represented by the given UUID is authenticated with xLogin.
     */
    boolean isAuthenticated(UUID uuid);
}
