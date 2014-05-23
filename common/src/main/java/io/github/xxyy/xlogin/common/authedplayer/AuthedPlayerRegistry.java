package io.github.xxyy.xlogin.common.authedplayer;

import net.md_5.bungee.api.Callback;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class which keeps track of all logged-in users.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.5.14
 */
public class AuthedPlayerRegistry {
    private final Map<UUID, AuthedPlayer> authedPlayers = new HashMap<>();
    private Callback<AuthedPlayer> authenticationCallback = null;

    public boolean isAuthenticated(UUID uuid){
        AuthedPlayer authedPlayer = authedPlayers.get(uuid);

        if(authedPlayer == null) {
            return false;
        }

        if(!authedPlayer.isValid() || !authedPlayer.isAuthenticated()) {
            remove(uuid);
            return false;
        }

        return true;
    }

    public AuthedPlayer.AuthenticationProvider getAuthenticationProvider(UUID uuid) {
        if(!isAuthenticated(uuid)) {
            return null;
        }

        return authedPlayers.get(uuid).getAuthenticationProvider();
    }

    public void registerAuthentication(AuthedPlayer authedPlayer){
        Validate.isTrue(authedPlayer.isAuthenticated(), "Tried to register non-authenticated player as authenticated!");
        Validate.isTrue(authedPlayer.isValid(), "Tried to register (literally) invalid player!");

        authedPlayers.put(UUID.fromString(authedPlayer.getUuid()), authedPlayer);

        if(authenticationCallback != null) {
            authenticationCallback.done(authedPlayer, null);
        }
    }

    public AuthedPlayer remove(UUID uuid){
        return authedPlayers.remove(uuid);
    }

    public void clear() {
        authedPlayers.clear();
    }

    public Callback<AuthedPlayer> getAuthenticationCallback() {
        return this.authenticationCallback;
    }

    public void setAuthenticationCallback(Callback<AuthedPlayer> authenticationCallback) {
        this.authenticationCallback = authenticationCallback;
    }
}
