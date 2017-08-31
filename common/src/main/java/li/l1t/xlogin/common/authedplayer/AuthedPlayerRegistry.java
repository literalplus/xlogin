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

package li.l1t.xlogin.common.authedplayer;

import com.google.common.collect.ImmutableSet;
import li.l1t.xlogin.common.api.XLoginRegistry;
import li.l1t.xlogin.common.api.XLoginRepository;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Class which keeps track of all logged-in users.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.5.14
 */
public class AuthedPlayerRegistry implements XLoginRegistry {
    protected static final Logger LOGGER = Logger.getLogger(AuthedPlayerRegistry.class.getName());
    protected final AuthedPlayerRepository repository;
    protected final Set<UUID> authedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    public AuthedPlayerRegistry(AuthedPlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isAuthenticated(@Nonnull UUID uuid) {
        if (!authedPlayers.contains(uuid)) {
            return false;
        }

        AuthedPlayer authedPlayer = repository.getProfile(uuid);
        if (authedPlayer == null || !authedPlayer.isValid() || !authedPlayer.isAuthenticated()) {
            this.authedPlayers.remove(uuid); //TODO: Does this actually every legitimately happen?
            LOGGER.info("Removing player from registry because null, invalid or something. " +
                    (authedPlayer == null ? "null" : authedPlayer.isValid() + ";a=" + authedPlayer.isAuthenticated()));
            return false;
        }

        return true;
    }

    public void registerAuthentication(@Nonnull AuthedPlayer authedPlayer) {
        Validate.isTrue(authedPlayer.isAuthenticated(), "Tried to register non-authenticated player as authenticated!");
        Validate.isTrue(authedPlayer.isValid(), "Tried to register (literally) invalid player!");

        authedPlayers.add(authedPlayer.getUniqueId());
    }

    /**
     * Forgets about the player represented by given UUID.
     * This action only affects local cache and does not persist to database.
     * This action, however, affects the associated {@link XLoginRepository}.
     *
     * @param uuid UUID of the player to forget about.
     */
    public void forget(UUID uuid) {
        authedPlayers.remove(uuid);
        repository.forget(uuid);
    }

    /**
     * @return a collection of all authenticated players' UUIDs
     */
    public Collection<UUID> getAuthenticatedPlayers() {
        return ImmutableSet.copyOf(authedPlayers);
    }

    public void clear() {
        authedPlayers.clear();
    }
}
