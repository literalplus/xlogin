/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.authedplayer;

import com.google.common.collect.ImmutableList;
import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.lib.intellij_annotations.Contract;
import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import org.apache.commons.lang3.Validate;

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
    @NotNull
    public static AuthedPlayer[] getByCriteria(@Nullable String input, AuthedPlayerRepository repository) {
        if (input == null){
            return new AuthedPlayer[0];
        }

        String query = "SELECT * FROM " +
                AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE ";

        if (input.startsWith("/")){
            query += "user_lastip=?";
        } else if (input.startsWith("%")){
            query += "username LIKE CONCAT(\"%\", ?, \"%\")";
        } else if (UtilUUID.isValidUUID(input)){
            query += "uuid=?";

            AuthedPlayer cached = getCached(repository, input);
            if (cached != null){
                return new AuthedPlayer[]{cached};
            }
        } else {
            query += "username=?";
        }

        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult(query, input).assertHasResultSet()) {
            List<AuthedPlayer> rtrn = new ArrayList<>();
            while (qr.rs().next()) {
                AuthedPlayer authedPlayer = getCached(repository, qr.rs().getString("uuid"));
                if (authedPlayer == null){
                    authedPlayer = getPlayerFromResultSet(qr.rs(), repository);
                }
                rtrn.add(authedPlayer);
            }

            return rtrn.toArray(new AuthedPlayer[rtrn.size()]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static AuthedPlayer get(@NotNull UUID uuid, String username, AuthedPlayerRepository repository) {
        //noinspection ConstantConditions
        return get(uuid, username, true, repository); //_,_,true -> !null
    }

    public static List<AuthedPlayer> getProfilesByName(String username, AuthedPlayerRepository repository) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE username = ? ORDER BY premium DESC", username).assertHasResultSet()) {
            return getProfilesFromResultSet(qr.rs(), repository);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static AuthedPlayer getProfile(@NotNull UUID uuid, AuthedPlayerRepository repository) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE uuid = ? ORDER BY premium DESC", uuid.toString()).assertHasResultSet()) {
            List<AuthedPlayer> profiles = getProfilesFromResultSet(qr.rs(), repository);

            if (profiles.isEmpty()){
                return null;
            } else if (profiles.size() == 1){
                return profiles.get(0);
            } else {
                throw new IllegalStateException("Multiple profile found for UUID " + uuid + "!");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<AuthedPlayer> getProfilesFromResultSet(@NotNull ResultSet rs, AuthedPlayerRepository repository) throws SQLException {
        ImmutableList.Builder<AuthedPlayer> builder = null;

        while (rs.next()) {
            AuthedPlayer authedPlayer = getPlayerFromResultSet(rs, repository);

            if (authedPlayer.isPremium()){
                return ImmutableList.of(authedPlayer);
            } else {
                if (builder == null){
                    builder = ImmutableList.builder();
                }

                builder.add(authedPlayer);
            }
        }

        return builder == null ? ImmutableList.<AuthedPlayer>of() : builder.build();
    }

    @NotNull
    private static AuthedPlayer getPlayerFromResultSet(@NotNull ResultSet rs, AuthedPlayerRepository repository) throws SQLException {
        return new AuthedPlayer(repository, rs.getString("uuid"), rs.getString("username"), rs.getString("password"),
                rs.getString("salt"), rs.getString("user_lastip"), rs.getBoolean("premium"), rs.getBoolean("ign_p_msg"),
                rs.getTimestamp("reg_date"), rs.getTimestamp("last_login"), rs.getBoolean("sessions_enabled"));
    }

    @Contract("_,_,true,_->!null")
    public static AuthedPlayer get(@NotNull UUID uuid, String username, boolean create, AuthedPlayerRepository repository) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE uuid = ?", uuid.toString())
                .assertHasResultSet()) {
            if (qr.rs().next()){
                return getPlayerFromResultSet(qr.rs(), repository);
            } else if (create){
//                PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
//                        "uuid=?, username=?", uuid.toString(), username);
                return new AuthedPlayer(repository, uuid.toString(), username, null, null, null, false, false,
                        new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), true);
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
    public static void save(@Nullable AuthedPlayer ap) {
        save(ap, false);
    }

    /**
     * Saves everything from an authed player <b>but the location</b>.
     *
     * @param ap                  player to save
     * @param saveUnauthenticated whether to force saving unauthenticated players
     */
    public static void save(@Nullable AuthedPlayer ap, boolean saveUnauthenticated) {
        if (ap == null){
            return;
        }

        Validate.isTrue(!ap.getRepository().isReadOnly(), "This data has been marked read-only by its repository.");
        Validate.isTrue(saveUnauthenticated || ap.isAuthenticated(), "Don't fucking save non-authed players, will you!");

        PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET " +
                        "username=?,password=?,salt=?,user_lastip=?,premium=?,ign_p_msg=?," +
                        "sessions_enabled=?, last_login=?, uuid=? ON DUPLICATE KEY UPDATE " +
                        "username=?,password=?,salt=?,user_lastip=?,premium=?,ign_p_msg=?," +
                        "sessions_enabled=?, last_login=?",
                ap.getName(), ap.getPassword(), ap.getSalt(), ap.getLastIp(), ap.isPremium(),
                ap.isDisabledPremiumMessage(), ap.isSessionsEnabled(), ap.getLastLoginDate(), ap.getUuid(),
                ap.getName(), ap.getPassword(), ap.getSalt(), ap.getLastIp(), ap.isPremium(),
                ap.isDisabledPremiumMessage(), ap.isSessionsEnabled(), ap.getLastLoginDate()
        );
    }

    /**
     * Deletes a player from db
     *
     * @param ap player
     */
    public static void delete(@Nullable AuthedPlayer ap) {
        if (ap == null){
            return;
        }

        PreferencesHolder.getSql().safelyExecuteUpdate("DELETE FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " WHERE uuid=?",
                ap.getUuid());
    }

    @Nullable
    private static AuthedPlayer getCached(@Nullable AuthedPlayerRepository repository, String input) {
        if (repository != null){
            UUID uuid = UtilUUID.getFromString(input);
            if (repository.hasCached(uuid)){
                return repository.getProfile(uuid);
            }
        }

        return null;
    }
}
