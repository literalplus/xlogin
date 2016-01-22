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

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Represents a location stored by xLogin, complete with server name.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.8.14
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationInfo {
    public static final String TABLE_NAME = "mt_main.xlogin_locations";

    @NotNull
    private final UUID playerId;
    @NotNull
    private final String serverName;
    @NotNull
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;

    /**
     * Loads a location from database which matches given parameters.
     *
     * @param authedPlayer the player to find the last location for
     * @param serverName   the server to get the location for
     * @return a LocationInfo representing the last location for given arguments or NULL if none.
     */
    protected static LocationInfo load(@NotNull AuthedPlayer authedPlayer, String serverName) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " +
                TABLE_NAME + " WHERE uuid=? AND server_name=?", authedPlayer.getUniqueId().toString(), serverName).vouchForResultSet()) {
            ResultSet rs = qr.rs();
            if (!rs.next()) {
                return null;
            } else {
                return new LocationInfo(authedPlayer.getUniqueId(),
                        rs.getString("server_name"), //Get correct casing
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z")
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Saves this location info to database, overriding previous values for uuid & server, if any.
     */
    public void save() {
        PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + TABLE_NAME +
                        " SET uuid=?, server_name=?, world=?, x=?, y=?, z=?" +
                        " ON DUPLICATE KEY UPDATE world=?, x=?, y=?, z=?",
                playerId.toString(), serverName, worldName, x, y, z, worldName, x, y, z);
    }
}
