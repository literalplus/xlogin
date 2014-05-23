package io.github.xxyy.xlogin.common.authedplayer;

import io.github.xxyy.xlogin.common.sql.EbeanManager;
import lombok.NonNull;

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

        boolean rtrn = EbeanManager.getEbean().createSqlQuery("SELECT COUNT(*) AS count FROM `"
                + AuthedPlayer.AUTH_DATA_TABLE_NAME + "` WHERE uuid=?")
                .setParameter(1, uuid.toString())
                .setMaxRows(1).findUnique()
                .getInteger("count") > 0;
        this.knownPlayers.put(uuid, rtrn);

        return rtrn;
    }

    /**
     * Fetches a player from the database.
     * Will not create new data if no object was found.
     *
     * @param uuid Unique Id of the player to find
     * @return An object representing the player or {@code null} if there is no such player.
     */
    public AuthedPlayer fetchPlayer(@NonNull UUID uuid) {
        return EbeanManager.getEbean().find(AuthedPlayer.class)
                .where()
                .eq("uuid", uuid.toString())
                .findUnique();
    }

    /**
     * Fetches a player from database or creates it if there is no such player.
     *
     * @param uuid UUID of the player to get
     * @param name Name of the player to get
     * @return An AuthedPLayer instance corresponding to the arguments
     */
    public AuthedPlayer getPlayer(@NonNull UUID uuid, @NonNull String name) {
        AuthedPlayer aplr = fetchPlayer(uuid);

        if (aplr == null) {
            aplr = new AuthedPlayer();

            aplr.setUuid(uuid.toString());
            aplr.setName(name);
            EbeanManager.getEbean().save(aplr);
        }

        if(!aplr.getName().equals(name)) {
            aplr.setName(name);
            EbeanManager.getEbean().save(aplr);
        }

        return aplr;
    }

    public void clear() {
        this.knownPlayers.clear();
        EbeanManager.getEbean().getServerCacheManager().clearAll();
    }

    public void updateKnown(UUID uuid, boolean knownState) {
        this.knownPlayers.put(uuid, knownState);
    }
}
