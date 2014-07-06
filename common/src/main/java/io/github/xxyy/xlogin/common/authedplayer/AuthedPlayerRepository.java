package io.github.xxyy.xlogin.common.authedplayer;

import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import lombok.NonNull;

import java.sql.SQLException;
import java.util.HashMap;
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
public class AuthedPlayerRepository {
    private Map<UUID, Boolean> knownPlayers = new HashMap<>();

    /**
     * Checks whether a player specified by a given UUID is known to the database.
     * Will make a query, so make sure to execute this async wherever possible.
     *
     * @param uuid Unique Id of the player to find
     * @return Whether that UUID is mapped to a player in the database.
     */
    public boolean isPlayerKnown(@NonNull UUID uuid) {
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
    public AuthedPlayer getPlayer(@NonNull UUID uuid, @NonNull String name) {
        AuthedPlayer aplr = AuthedPlayerFactory.get(uuid, name);

        if (!aplr.getName().equals(name)) {
            aplr.setName(name);
            AuthedPlayerFactory.save(aplr);
        }

        return aplr;
    }

    /**
     * Gets a UUID that matches given name. If there are multiple entries for that name,
     * premium ones (There can only be one) are preferred.
     * If all returned names are cracked, a random one is returned. (This applies to very few database entries)
     * @param input {Name of the player to get. Casing is ignored.} or {a valid UUID String}
     * @return UUID of the matched player, as used by xLogin or NULL if no UUID is recorded for that name.
     */
    public UUID getUniqueId(@NonNull String input) {
        if(UtilUUID.isValidUUID(input)) {
            return UtilUUID.getFromString(input); //Since the check method checks for Mojang-style UUIDs too, we need to treat those as valid.
        }
        return AuthedPlayerFactory.getIdByName(input);
    }

    public void deletePlayer(@NonNull AuthedPlayer ap) {
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
    public AuthedPlayer forceGetPlayer(@NonNull UUID uuid, @NonNull String name) {
        AuthedPlayer aplr = AuthedPlayerFactory.forceGet(uuid, name);

        if (!aplr.getName().equals(name)) {
            aplr.setName(name);
            AuthedPlayerFactory.save(aplr);
        }

        return aplr;
    }

    public void clear() {
        this.knownPlayers.clear();
        AuthedPlayerFactory.clear();
    }

    public void forget(UUID uuid) {
        updateKnown(uuid, null);
        this.knownPlayers.remove(uuid);
        AuthedPlayerFactory.remove(uuid);
    }

    public void updateKnown(UUID uuid, Boolean knownState) {
        if (knownState == null) {
            this.knownPlayers.remove(uuid);
        } else {
            this.knownPlayers.put(uuid, knownState);
        }
    }
}
