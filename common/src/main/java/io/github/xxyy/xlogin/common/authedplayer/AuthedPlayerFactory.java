package io.github.xxyy.xlogin.common.authedplayer;

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
public final class AuthedPlayerFactory {
    private static final HashMap<UUID, AuthedPlayer> players = new HashMap<>();

    public static AuthedPlayer getCache(UUID uuid) {
        return players.get(uuid);
    }

    public static AuthedPlayer get(UUID uuid, String username) {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        }

        return forceGet(uuid, username);
    }

    public static AuthedPlayer forceGet(UUID uuid, String username) {
        try (QueryResult qr = PreferencesHolder.sql.executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE uuid = ?", uuid.toString())
                .assertHasResultSet()) {
            ResultSet rs = qr.rs();
            if (rs.next()) {
                AuthedPlayer authedPlayer = new AuthedPlayer(rs.getString("uuid"), rs.getString("username"), rs.getString("password"),
                        rs.getString("salt"), rs.getString("user_lastip"), rs.getBoolean("premium"), rs.getBoolean("ign_p_msg"),
                        rs.getTimestamp("reg_date"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getString("world"),
                        rs.getBoolean("sessions_enabled"));
                players.put(uuid, authedPlayer);
                return authedPlayer;
            } else {
                PreferencesHolder.sql.safelyExecuteUpdate("INSERT INTO " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
                        "uuid=?, username=?", uuid.toString(), username);
                AuthedPlayer authedPlayer = new AuthedPlayer(uuid.toString(), username, null, null, null, false, false, new Timestamp(System.currentTimeMillis()),
                        0, 0, 0, null, true);
                players.put(uuid, authedPlayer);
                return authedPlayer;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save(AuthedPlayer ap) {
        PreferencesHolder.sql.safelyExecuteUpdate("UPDATE " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
                        "username=?,password=?,salt=?,user_lastip=?,premium=?,ign_p_msg=?,x=?,y=?,z=?," +
                        "world=?,sessions_enabled=? WHERE uuid=?",
                ap.getName(), ap.getPassword(), ap.getSalt(), ap.getLastIp(), ap.isPremium(),
                ap.isDisabledPremiumMessage(), ap.getLastLogoutBlockX(), ap.getLastLogoutBlockY(),
                ap.getLastLogoutBlockZ(), ap.getLastWorldName(), ap.isSessionsEnabled(), ap.getUuid()
        );
    }

    public static void remove(UUID uuid) {
        players.remove(uuid);
    }
}
