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

package li.l1t.xlogin.spigot.authedplayer;

import li.l1t.xlogin.common.authedplayer.AuthedPlayerRegistry;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerRepository;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * A Spigot-specific implementation of {@link AuthedPlayerRegistry}. Specifically doesn't check if players are
 * known to the repository when checking authentication.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30/09/15
 */ //to allow us to retrieve player data async on auth, speeding up login
public class SpigotPlayerRegistry extends AuthedPlayerRegistry {
    public SpigotPlayerRegistry(AuthedPlayerRepository repository) {
        super(repository);
    }

    @Override
    public boolean isAuthenticated(@Nonnull UUID uuid) {
        return authedPlayers.contains(uuid);
    }

    public void registerAuthentication(@Nonnull UUID uuid) {
        authedPlayers.add(uuid);
    }
}
