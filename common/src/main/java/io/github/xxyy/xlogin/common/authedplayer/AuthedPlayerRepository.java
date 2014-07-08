package io.github.xxyy.xlogin.common.authedplayer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;
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
public class AuthedPlayerRepository {
    private Map<UUID, Boolean> knownPlayers = new HashMap<>();
    private Map<String, List<XLoginProfile>> nameProfilesCache = new CaseInsensitiveMap<>();
    private Map<UUID, XLoginProfile> idProfileCache = new HashMap<>();

    /**
     * Checks whether a player specified by a given UUID is known to the database.
     * Will make a query, so make sure to execute this async wherever possible.
     *
     * @param uuid Unique Id of the player to find
     * @return Whether that UUID is mapped to a player in the database.
     */
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
    public AuthedPlayer getPlayer(@NotNull UUID uuid, @NotNull String name) {
        AuthedPlayer aplr = AuthedPlayerFactory.get(uuid, name);

        if (!aplr.getName().equals(name)) {
            aplr.setName(name);
            AuthedPlayerFactory.save(aplr);
        }

        return aplr;
    }

    /**
     * Gets profiles that match the given user name. Premium players are returned first.
     *
     * @param name Name of the player to get. Casing is ignored.
     * @return List of known profiles for that criteria.
     *
     * @see #getProfiles(String)
     */
    @NotNull
    public List<XLoginProfile> getProfilesByName(@NotNull String name) {
        List<XLoginProfile> result = nameProfilesCache.get(name);

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

    /**
     * Gets profiles that match the given criteria. Premium players are returned first.
     * If you are certain that the input is not a UUID, use {@link #getProfilesByName(String)}
     *
     * @param input {Name of the player to get. Casing is ignored.} or {a valid UUID String}
     * @return List of known profiles for that criteria.
     */
    @NotNull
    public List<XLoginProfile> getProfiles(@NotNull String input) {
        List<XLoginProfile> result = nameProfilesCache.get(input);

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

    /**
     * Gets the profile for the given UUID.
     *
     * @param uuid UUID of the profile to get
     * @return Profile info for requested UUID or NULL if there's no such profile.
     */
    public XLoginProfile getProfile(@NotNull UUID uuid) {
        XLoginProfile result = idProfileCache.get(uuid);

        if (result == null) {
            result = overrideProfile(uuid);
        }

        return result;
    }

    //Gets a profile and overrides cache, if existent.
    private XLoginProfile overrideProfile(UUID uuid) {
        XLoginProfile result;

        result = AuthedPlayerFactory.getProfile(uuid);
        idProfileCache.put(uuid, result);

        return result;
    }

    public void deletePlayer(@NotNull AuthedPlayer ap) {
        AuthedPlayerFactory.delete(ap);
        forget(UUID.fromString(ap.getUuid()));
    }

    /**
     * Fetches a player from database or creates it if there is no such player.
     * Overrides local cache.
     *
     * @param uuid UUID of the player to get
     * @param name Name of the player to get
     * @return An AuthedPLayer instance corresponding to the arguments
     */
    public AuthedPlayer forceGetPlayer(@NotNull UUID uuid, @NotNull String name) {
        AuthedPlayer aplr = AuthedPlayerFactory.forceGet(uuid, name);

        if (!aplr.getName().equals(name)) {
            aplr.setName(name);
            AuthedPlayerFactory.save(aplr);
        }

        return aplr;
    }

    public void clear() {
        this.knownPlayers.clear();
        this.idProfileCache.clear();
        this.nameProfilesCache.clear();
        AuthedPlayerFactory.clear();
    }

    public void forget(UUID uuid) {
        this.knownPlayers.remove(uuid);
        this.idProfileCache.remove(uuid);
        AuthedPlayerFactory.remove(uuid);
    }

    public void refreshProfile(UUID uuid) {
        XLoginProfile profile = overrideProfile(uuid);

        if (profile == null) {
            this.idProfileCache.remove(uuid);
        } else {
            this.updateProfile(profile);
        }
    }

    public void forgetProfile(XLoginProfile profile) {
        this.idProfileCache.remove(profile.getUniqueId());
        this.nameProfilesCache.remove(profile.getName());
    }

    public void updateProfile(XLoginProfile profile) {
        this.idProfileCache.put(profile.getUniqueId(), profile);
        //If we have a casing of the name, keep that to prevent inconsistencies
        this.nameProfilesCache.put(profile.getName(), Lists.newArrayList(profile));
    }
}
