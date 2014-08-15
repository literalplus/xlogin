package io.github.xxyy.xlogin.common.authedplayer;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;

import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Creates instances of {@link AuthedPlayer}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
public final class AuthedPlayerFactory {

    /**
     * Gets AuthedPlayers.
     *
     * @param input      Either an UUID, part of a name to match all players having that part in their name or an
     *                   IP, starting with /.
     * @param repository the repository to use to get already loaded players
     * @return All AuthedPlayer that match given criteria.
     */
    public static AuthedPlayer[] getByCriteria(String input, AuthedPlayerRepository repository) {
        if (input == null) {
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

            AuthedPlayer cached = getCached(repository, input);
            if (cached != null) {
                return new AuthedPlayer[]{cached};
            }
        } else {
            query += "username=?";
        }

        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult(query, input).assertHasResultSet()) {
            List<AuthedPlayer> rtrn = new ArrayList<>();
            while (qr.rs().next()) {
                AuthedPlayer authedPlayer = getCached(repository, qr.rs().getString("uuid"));
                if (authedPlayer == null) {
                    authedPlayer = getPlayerFromResultSet(qr.rs());
                }
                rtrn.add(authedPlayer);
            }

            return rtrn.toArray(new AuthedPlayer[rtrn.size()]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthedPlayer get(UUID uuid, String username) {
        return get(uuid, username, true);
    }

    public static List<AuthedPlayer> getProfilesByName(String username) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE username = ? ORDER BY premium DESC", username).assertHasResultSet()) {
            return getProfilesFromResultSet(qr.rs());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static AuthedPlayer getProfile(UUID uuid) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE uuid = ? ORDER BY premium DESC", uuid.toString()).assertHasResultSet()) {
            List<AuthedPlayer> profiles = getProfilesFromResultSet(qr.rs());

            if (profiles.isEmpty()) {
                return null;
            } else if (profiles.size() == 1) {
                return profiles.get(0);
            } else {
                throw new IllegalStateException("Multiple profile found for UUID " + uuid + "!");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<AuthedPlayer> getProfilesFromResultSet(ResultSet rs) throws SQLException {
        ImmutableList.Builder<AuthedPlayer> builder = null;

        while (rs.next()) {
            AuthedPlayer authedPlayer = getPlayerFromResultSet(rs);

            if (authedPlayer.isPremium()) {
                return ImmutableList.of(authedPlayer);
            } else {
                if (builder == null) {
                    builder = ImmutableList.builder();
                }

                builder.add(authedPlayer);
            }
        }

        return builder == null ? ImmutableList.<AuthedPlayer>of() : builder.build();
    }

    private static AuthedPlayer getPlayerFromResultSet(ResultSet rs) throws SQLException {
        return new AuthedPlayer(rs.getString("uuid"), rs.getString("username"), rs.getString("password"),
                rs.getString("salt"), rs.getString("user_lastip"), rs.getBoolean("premium"), rs.getBoolean("ign_p_msg"),
                rs.getTimestamp("reg_date"), rs.getBoolean("sessions_enabled"));
    }

    public static AuthedPlayer get(UUID uuid, String username, boolean create) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE uuid = ?", uuid.toString())
                .assertHasResultSet()) {
            if (qr.rs().next()) {
                return getPlayerFromResultSet(qr.rs());
            } else if (create) {
//                PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
//                        "uuid=?, username=?", uuid.toString(), username);
                return new AuthedPlayer(uuid.toString(), username, null, null, null, false, false, new Timestamp(System.currentTimeMillis()), true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Saves everything from an authed player <b>but the location</b>.
     *
     * @param ap player to save
     */
    public static void save(AuthedPlayer ap) {
        if (ap == null) {
            return;
        }

        Validate.isTrue(ap.isAuthenticated(), "Don't fucking save non-authed players, will you!!");

        PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
                        "username=?,password=?,salt=?,user_lastip=?,premium=?,ign_p_msg=?," +
                        "sessions_enabled=?, uuid=? ON DUPLICATE KEY UPDATE " +
                        "username=?,password=?,salt=?,user_lastip=?,premium=?,ign_p_msg=?," +
                        "sessions_enabled=?",
                ap.getName(), ap.getPassword(), ap.getSalt(), ap.getLastIp(), ap.isPremium(),
                ap.isDisabledPremiumMessage(), ap.isSessionsEnabled(), ap.getUuid(),
                ap.getName(), ap.getPassword(), ap.getSalt(), ap.getLastIp(), ap.isPremium(),
                ap.isDisabledPremiumMessage(), ap.isSessionsEnabled()
        );
    }

    /**
     * Deletes a player from db
     *
     * @param ap player
     */
    public static void delete(AuthedPlayer ap) {
        if (ap == null) {
            return;
        }

        PreferencesHolder.getSql().safelyExecuteUpdate("DELETE FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE uuid=?",
                ap.getUuid());
    }

    private static AuthedPlayer getCached(AuthedPlayerRepository repository, String input) {
        if (repository != null) {
            UUID uuid = UtilUUID.getFromString(input);
            if (repository.hasCached(uuid)) {
                return repository.getProfile(uuid);
            }
        }

        return null;
    }
}
