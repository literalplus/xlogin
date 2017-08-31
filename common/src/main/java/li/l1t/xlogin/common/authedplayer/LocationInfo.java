/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.l1t.xlogin.common.authedplayer;

import li.l1t.common.sql.QueryResult;
import li.l1t.xlogin.common.PreferencesHolder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nonnull;
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

    @Nonnull
    private final UUID playerId;
    @Nonnull
    private final String serverName;
    @Nonnull
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
    protected static LocationInfo load(@Nonnull AuthedPlayer authedPlayer, String serverName) {
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
