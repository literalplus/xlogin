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

package li.l1t.xlogin.bungee.punishment.warn;

import com.google.common.collect.ImmutableList;
import li.l1t.common.sql.QueryResult;
import li.l1t.common.sql.UpdateResult;
import li.l1t.xlogin.common.PreferencesHolder;
import li.l1t.xlogin.common.api.punishments.XLoginWarning;
import net.md_5.bungee.api.connection.Server;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Creates WarningInfo objects.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
final class WarningInfoFactory {
    private WarningInfoFactory() {

    }

    public static WarningInfo fetch(int id) {
        List<WarningInfo> results = fetch("warn_id=?", id);

        if (results.size() == 0) {
            return null;
        } else {
            return results.get(0); //When we get more than one value for a PRIMARY KEY, something is so wrong that an exception won't matter at all.
        }
    }

    public static List<WarningInfo> fetchByTarget(UUID target) {
        return fetch("target_uuid=?", target.toString());
    }

    public static List<WarningInfo> fetchBySource(UUID source) {
        return fetch("source_uuid=?", source.toString());
    }

    public static WarningInfo fetchLastIssuedBy(UUID source) {
        List<WarningInfo> results = fetch("source_uuid=? ORDER BY timestamp DESC LIMIT 1", source.toString());

        if (results.size() == 0) {
            return null;
        } else {
            return results.get(0); //Again, when LIMIT 1 fails, we got bigger problems
        }
    }

    private static List<WarningInfo> fetch(String where, Object whereParam) {
        ImmutableList.Builder<WarningInfo> results = ImmutableList.builder();
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult(
                "SELECT warn_id,target_uuid,source_uuid,reason,source_server,timestamp,state FROM " +
                        WarningInfo.WARN_TABLE_NAME + " WHERE " + where, whereParam).vouchForResultSet()) {
            while (qr.rs().next()) {
                results.add(new WarningInfo(
                        qr.rs().getInt("warn_id"),
                        UUID.fromString(qr.rs().getString("target_uuid")),
                        UUID.fromString(qr.rs().getString("source_uuid")),
                        qr.rs().getString("reason"),
                        qr.rs().getString("source_server"),
                        qr.rs().getTimestamp("timestamp"),
                        XLoginWarning.WarningState.values()[qr.rs().getInt("state")]
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        return results.build();
    }

    public static WarningInfo create(UUID targetId, UUID sourceId, Server sourceServer, String reason) {
        String sourceServerName = sourceServer == null ? "CONSOLE" : sourceServer.getInfo().getName();
        try {
            UpdateResult ur = PreferencesHolder.getSql().executeUpdateWithGenKeys("INSERT INTO " + WarningInfo.WARN_TABLE_NAME + " SET " +
                            "target_uuid=?,source_uuid=?,reason=?,source_server=?",
                    targetId.toString(), sourceId.toString(), reason,
                    sourceServerName);
            ur.vouchForGeneratedKeys();

            if (ur.gk() == null || !ur.gk().next()) {
                return null;
            }

            return new WarningInfo(
                    ur.gk().getInt(1), targetId, sourceId, reason, sourceServerName,
                    new Timestamp(System.currentTimeMillis()), XLoginWarning.WarningState.VALID
            );
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void save(WarningInfo warningInfo) {
        PreferencesHolder.getSql().safelyExecuteUpdate("UPDATE " + WarningInfo.WARN_TABLE_NAME + " SET reason=?, state=? WHERE warn_id=?",
                warningInfo.getReason(), warningInfo.getState().ordinal(), warningInfo.getId());
    }

    public static void delete(WarningInfo warningInfo) {
        PreferencesHolder.getSql().safelyExecuteUpdate("DELETE FROM " + WarningInfo.WARN_TABLE_NAME + " WHERE warn_id=?", warningInfo.getId());
        warningInfo.setState(XLoginWarning.WarningState.DELETED);
    }
}
