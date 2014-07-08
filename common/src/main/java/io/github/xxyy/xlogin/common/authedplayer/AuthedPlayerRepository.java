package io.github.xxyy.xlogin.common.authedplayer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.api.XLoginRepository;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the creation of {@link AuthedPlayer}s.
 * Either creates them newly or gets them from database.
 * New players are written to database immediately.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 15.5.14
 */
@SuppressWarnings("UnusedDeclaration") //API declarations are not used by xLogin internally
public class AuthedPlayerRepository implements XLoginRepository {
    private Map<UUID, Boolean> knownPlayers = new HashMap<>();
    private Map<String, List<AuthedPlayer>> nameProfilesCache = new CaseInsensitiveMap<>();
    private Map<UUID, AuthedPlayer> idProfileCache = new HashMap<>();

    @Override
    public boolean isPlayerKnown(@NotNull UUID uuid) {
        if (knownPlayers.containsKey(uuid)) {
            return knownPlayers.get(uuid);
        }
        boolean rtrn;

        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT COUNT(*) FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE (premium = 1 OR password IS NOT NULL) AND uuid=?", uuid.toString()).assertHasResultSet()) {
            rtrn = qr.rs().next() && qr.rs().getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.knownPlayers.put(uuid, rtrn);

        return rtrn;
    }

    /**
     * Fetches a player from database or creates it if there is no such player.
     *
     * @param uuid UUID of the player to get
     * @param name Name of the player to get
     * @return An AuthedPlayer instance corresponding to the arguments
     */
    public AuthedPlayer getProfile(@NotNull UUID uuid, @NotNull String name) {
        AuthedPlayer authedPlayer = idProfileCache.get(uuid);

        if (authedPlayer == null) {
            authedPlayer = AuthedPlayerFactory.get(uuid, name);

            if (!authedPlayer.getName().equals(name)) {
                authedPlayer.setName(name);
                AuthedPlayerFactory.save(authedPlayer);
            }
        }

        return authedPlayer;
    }

    @Override
    @NotNull
    public List<AuthedPlayer> getProfilesByName(@NotNull String name) {
        List<AuthedPlayer> result = nameProfilesCache.get(name);

        if (result == null) {
            result = AuthedPlayerFactory.getProfilesByName(name);


            if (result.size() == 1) {
                updateProfile(result.get(0));
            } else {
                nameProfilesCache.put(name, result);
            }
        }

        return result;
    }

    @Override
    @NotNull
    public List<AuthedPlayer> getProfiles(@NotNull String input) {
        List<AuthedPlayer> result = nameProfilesCache.get(input);

        if (result == null) {
            if (UtilUUID.isValidUUID(input)) {
                //Since the check method checks for Mojang-style UUIDs too, we need to treat those as valid.
                result = ImmutableList.of(getProfile(UtilUUID.getFromString(input)));
                nameProfilesCache.put(input, result);
            } else {
                result = getProfilesByName(input);
            }
        }

        return result;
    }

    @Override
    public AuthedPlayer getProfile(@NotNull UUID uuid) {
        AuthedPlayer result = idProfileCache.get(uuid);

        if (result == null) {
            result = overrideProfile(uuid);
        }

        return result;
    }

    //Gets a profile and overrides cache, if existent.
    private AuthedPlayer overrideProfile(UUID uuid) {
        AuthedPlayer result;

        result = AuthedPlayerFactory.getProfile(uuid);
        idProfileCache.put(uuid, result);

        return result;
    }

    public void deletePlayer(@NotNull AuthedPlayer ap) {
        AuthedPlayerFactory.delete(ap);
        forget(ap.getUniqueId());
    }

    /**
     * Refreshes the information about a player.
     *
     * @param uuid UUID of the player to get
     * @param name Name of the player to get
     * @return An AuthedPlayer instance corresponding to the arguments
     */
    public AuthedPlayer refreshPlayer(@NotNull UUID uuid, @NotNull String name) {
        AuthedPlayer oldPlayer = idProfileCache.get(uuid);

        if(oldPlayer != null) {
            oldPlayer.setValid(false);
        }

        AuthedPlayer refreshedPlayer = AuthedPlayerFactory.get(uuid, name);

        if (!refreshedPlayer.getName().equals(name)) {
            refreshedPlayer.setName(name);
            AuthedPlayerFactory.save(refreshedPlayer);
        }

        updateProfile(refreshedPlayer);

        return refreshedPlayer;
    }

    public void clear() {
        this.knownPlayers.clear();
        this.idProfileCache.clear();
        this.nameProfilesCache.clear();
    }

    public void forget(UUID uuid) {
        AuthedPlayer authedPlayer = this.idProfileCache.get(uuid);

        if (authedPlayer == null) {
            this.knownPlayers.remove(uuid);
        } else {
            forgetProfile(authedPlayer);
        }
    }

    @Override
    public void refreshProfile(UUID uuid) {
        AuthedPlayer profile = overrideProfile(uuid);

        if (profile == null) {
            this.idProfileCache.remove(uuid);
        } else {
            this.updateProfile(profile);
        }
    }

    public void forgetProfile(AuthedPlayer profile) {
        this.knownPlayers.remove(profile.getUniqueId());
        this.idProfileCache.remove(profile.getUniqueId());
        this.nameProfilesCache.remove(profile.getName());
    }

    public void updateProfile(AuthedPlayer profile) {
        this.idProfileCache.put(profile.getUniqueId(), profile);
        //If we have a casing of the name, keep that to prevent inconsistencies
        this.nameProfilesCache.put(profile.getName(), Lists.newArrayList(profile));
    }
}
