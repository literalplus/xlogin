/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.punishment.warn;

import com.google.common.collect.ImmutableList;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.common.sql.UpdateResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.api.punishments.XLoginWarning;
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
