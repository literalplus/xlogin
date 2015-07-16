package io.github.xxyy.xlogin.common.authedplayer;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.Validate;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.xlogin.common.api.XLoginRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Class which keeps track of all logged-in users.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.5.14
 */
public class AuthedPlayerRegistry implements XLoginRegistry {
    private static final Logger LOGGER = Logger.getLogger(AuthedPlayerRegistry.class.getName());
    private final AuthedPlayerRepository repository;
    private final List<UUID> authedPlayers = new ArrayList<>();

    public AuthedPlayerRegistry(AuthedPlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isAuthenticated(@NotNull UUID uuid) {
        if (!authedPlayers.contains(uuid)) {
            return false;
        }

        AuthedPlayer authedPlayer = repository.getProfile(uuid);
        if (authedPlayer == null || !authedPlayer.isValid() || !authedPlayer.isAuthenticated()) {
            this.authedPlayers.remove(uuid);
//            LOGGER.info("Removing player from registry because null, invalid or something. " +
//                    (authedPlayer == null ? "null" : authedPlayer.isValid() + ";a=" + authedPlayer.isAuthenticated()));
            return false;
        }

        return true;
    }

    public void registerAuthentication(@NotNull AuthedPlayer authedPlayer) {
        Validate.isTrue(authedPlayer.isAuthenticated(), "Tried to register non-authenticated player as authenticated!");
        Validate.isTrue(authedPlayer.isValid(), "Tried to register (literally) invalid player!");

        authedPlayers.add(authedPlayer.getUniqueId());
    }

    /**
     * Forgets about the player represented by given UUID.
     * This action only affects local cache and does not persist to database.
     * This action, however, affects the associated {@link io.github.xxyy.xlogin.common.api.XLoginRepository}.
     * @param uuid UUID of the player to forget about.
     */
    public void forget(UUID uuid){
        authedPlayers.remove(uuid);
        repository.forget(uuid);
    }

    /**
     * @return a collection of all authenticated players' UUIDs
     */
    public Collection<UUID> getAuthenticatedPlayers() {
        return ImmutableList.copyOf(authedPlayers);
    }

    public void clear() {
        authedPlayers.clear();
    }
}
