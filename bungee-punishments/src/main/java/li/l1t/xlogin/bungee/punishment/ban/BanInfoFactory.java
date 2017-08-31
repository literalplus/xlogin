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

package li.l1t.xlogin.bungee.punishment.ban;

import com.google.common.collect.ImmutableList;
import li.l1t.common.sql.QueryResult;
import li.l1t.xlogin.common.PreferencesHolder;
import net.md_5.bungee.api.connection.Server;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Handles creating and fetching of ban list entries.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 27.8.14
 */
final class BanInfoFactory {
    private BanInfoFactory() {

    }

    public static BanInfo fetchByTarget(BanModule manager, UUID target) {
        List<BanInfo> results = fetch(manager, "target_uuid=?", target.toString());

        if (results.size() == 0) {
            return null;
        } else {
            return results.get(0); //When LIMIT 1 fails, we got bigger problems
        }
    }

    public static List<BanInfo> fetchBySource(BanModule manager, UUID source) {
        return fetch(manager, "source_uuid=?", source.toString());
    }

    public static BanInfo fetchLastIssuedBy(BanModule manager, UUID source) {
        List<BanInfo> results = fetch(manager, "source_uuid=? ORDER BY timestamp DESC LIMIT 1", source.toString());

        if (results.size() == 0) {
            return null;
        } else {
            return results.get(0); //When LIMIT 1 fails, we got bigger problems
        }
    }

    private static List<BanInfo> fetch(BanModule manager, String where, Object whereParam) {
        ImmutableList.Builder<BanInfo> results = ImmutableList.builder();
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult(
                "SELECT target_uuid,source_uuid,reason,source_server,timestamp,expiry_time FROM " +
                        BanInfo.BAN_TABLE_NAME + " WHERE " + where, whereParam).vouchForResultSet()) {
            while (qr.rs().next()) {
                results.add(new BanInfo(
                        manager,
                        UUID.fromString(qr.rs().getString("target_uuid")),
                        UUID.fromString(qr.rs().getString("source_uuid")),
                        qr.rs().getString("reason"),
                        qr.rs().getString("source_server"),
                        qr.rs().getTimestamp("timestamp"),
                        qr.rs().getTimestamp("expiry_time")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        return results.build();
    }

    public static BanInfo create(BanModule manager, UUID targetId, UUID sourceId, Server sourceServer, String reason, @Nullable Date expiryDate) {
        return create(manager, targetId, sourceId,
                sourceServer == null ? "CONSOLE" : sourceServer.getInfo().getName(),
                reason, expiryDate);
    }

    public static BanInfo create(BanModule manager, UUID targetId, UUID sourceId, String sourceServerName, String reason, @Nullable Date expiryDate) {
        Timestamp expiryTimestamp = expiryDate == null ? null : new Timestamp(expiryDate.getTime());
        PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + BanInfo.BAN_TABLE_NAME + " SET " +
                        "target_uuid=?,source_uuid=?,reason=?,source_server=?,expiry_time=? ON DUPLICATE KEY UPDATE " +
                        "source_uuid=?,reason=?,source_server=?,expiry_time=?",
                targetId.toString(), sourceId.toString(), reason, sourceServerName, expiryTimestamp,
                sourceId.toString(), reason, sourceServerName, expiryTimestamp);

        return new BanInfo( manager, targetId, sourceId, reason,
                sourceServerName, new Timestamp(System.currentTimeMillis()),
                expiryTimestamp);
    }

    public static void save(BanInfo banInfo) {
        PreferencesHolder.getSql().safelyExecuteUpdate("UPDATE " + BanInfo.BAN_TABLE_NAME + " SET reason=?, expiry_time=? WHERE target_uuid=?",
                banInfo.getReason(), banInfo.getExpiryTime(), banInfo.getTargetId().toString());
    }

    public static void delete(BanInfo banInfo) {
        PreferencesHolder.getSql().safelyExecuteUpdate("DELETE FROM " + BanInfo.BAN_TABLE_NAME + " WHERE target_uuid=?", banInfo.getTargetId().toString());
    }
}
