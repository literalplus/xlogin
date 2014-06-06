package io.github.xxyy.xlogin.common.authedplayer;

import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Gets AuthedPlayers.
     *
     * @param input Either an UUID, part of a name to match all players having that part in their name or an
     *              IP, starting with /.
     * @return All AuthedPlayer that match given criteria.
     */
    public static AuthedPlayer[] getByCriteria(String input) {
        if(input == null) {
            return new AuthedPlayer[0];
        }

        String query = "SELECT uuid,username,password,salt,user_lastip,premium,ign_p_msg,reg_date,x,y,z,world,sessions_enabled FROM " +
                AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE ";

        if (input.startsWith("/")) {
            query += "user_lastip=?";
        } else if (input.startsWith("%")) {
            query += "username LIKE CONCAT(\"%\", ?, \"%\")";
        } else if (UtilUUID.isValidUUID(input)) {
            query += "uuid=?";
        } else {
            query += "username=?";
        }

        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult(query, input).assertHasResultSet()) {
            ResultSet rs = qr.rs();
            List<AuthedPlayer> rtrn = new ArrayList<>();
            while (rs.next()) {
                AuthedPlayer authedPlayer = new AuthedPlayer(rs.getString("uuid"), rs.getString("username"), rs.getString("password"),
                        rs.getString("salt"), rs.getString("user_lastip"), rs.getBoolean("premium"), rs.getBoolean("ign_p_msg"),
                        rs.getTimestamp("reg_date"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getString("world"),
                        rs.getBoolean("sessions_enabled"));
                players.put(UUID.fromString(authedPlayer.getUuid()), authedPlayer);
                rtrn.add(authedPlayer);
            }

            return rtrn.toArray(new AuthedPlayer[rtrn.size()]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthedPlayer get(UUID uuid, String username) {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        }

        return forceGet(uuid, username);
    }

    public static AuthedPlayer forceGet(UUID uuid, String username) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE uuid = ?", uuid.toString())
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
                PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
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

    /**
     * Saves everything from an authed player <b>but the location</b>.
     *
     * @param ap player to save
     */
    public static void save(AuthedPlayer ap) {
        if(ap == null) {
            return;
        }

        PreferencesHolder.getSql().safelyExecuteUpdate("UPDATE " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
                        "username=?,password=?,salt=?,user_lastip=?,premium=?,ign_p_msg=?," +
                        "sessions_enabled=? WHERE uuid=?",
                ap.getName(), ap.getPassword(), ap.getSalt(), ap.getLastIp(), ap.isPremium(),
                ap.isDisabledPremiumMessage(), ap.isSessionsEnabled(), ap.getUuid()
        );
    }

    /**
     * Deletes a player from db
     * @param ap player
     */
    public static void delete(AuthedPlayer ap) {
        if(ap == null) {
            return;
        }

        PreferencesHolder.getSql().safelyExecuteUpdate("DELETE FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE uuid=?",
                ap.getUuid());
        remove(UUID.fromString(ap.getUuid()));
    }

    public static void remove(UUID uuid) {
        players.remove(uuid);
    }

    public static void clear() {
        players.clear();
    }
}
