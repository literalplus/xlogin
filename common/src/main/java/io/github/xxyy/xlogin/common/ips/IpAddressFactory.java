/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.lib.intellij_annotations.Contract;
import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.common.PreferencesHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
public class IpAddressFactory {
    private static final Map<String, IpAddress> cache = new HashMap<>();

    @Contract("null -> null")
    public static IpAddress get(@Nullable String ipString) {
        if(ipString == null) {
            return null;
        }

        if (cache.containsKey(ipString)) {
            return cache.get(ipString);
        }

        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM " + IpAddress.TABLE_NAME + " WHERE ip=?", ipString).assertHasResultSet()) {
            ResultSet rs = qr.rs();

            if (rs.next()) {
                IpAddress ipAddress = new IpAddress(ipString, rs.getInt("maxusers"));
                cache.put(ipString, ipAddress);
                return ipAddress;
            } else {
                PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO " + IpAddress.TABLE_NAME + " SET ip=?,maxusers=?", ipString, PreferencesHolder.getMaxUsersPerIp());
                return new IpAddress(ipString, PreferencesHolder.getMaxUsersPerIp());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save(@Nullable IpAddress toSave) {
        if(toSave == null) {
            return;
        }

        PreferencesHolder.getSql().safelyExecuteUpdate("UPDATE mt_main.xlogin_ips SET maxusers=? WHERE ip=?",
                toSave.getMaxUsers(), toSave.getIp());
        cache.put(toSave.getIp(), toSave);
    }

    public static boolean exists(String ipString) {
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT COUNT(*) AS cnt FROM " + IpAddress.TABLE_NAME + " " +
                "WHERE ip=?", ipString).assertHasResultSet()) {
            return qr.rs().next() && qr.rs().getInt("cnt") > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void free(@NotNull String ipString, int newSlotAmount) {
        IpAddress ip = get(ipString);
        ip.setMaxUsers(newSlotAmount);
        save(ip);
    }

    public static void removeFromCache(String ipString) {
        cache.remove(ipString);
    }

    public static void clear() {
        cache.clear();
    }
}
