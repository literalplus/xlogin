package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.common.sql.QueryResult;
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

    public static IpAddress get(String ipString) {
        if(ipString == null) {
            return null;
        }

        if (cache.containsKey(ipString)) {
            return cache.get(ipString);
        }

        try (QueryResult qr = PreferencesHolder.sql.executeQueryWithResult("SELECT * FROM " + IpAddress.TABLE_NAME + " WHERE ip=?", ipString).assertHasResultSet()) {
            ResultSet rs = qr.rs();

            if (rs.next()) {
                IpAddress ipAddress = new IpAddress(ipString, rs.getInt("maxusers"), rs.getBoolean("sessions_on"));
                cache.put(ipString, ipAddress);
                return ipAddress;
            } else {
                PreferencesHolder.sql.safelyExecuteUpdate("INSERT INTO " + IpAddress.TABLE_NAME + " SET ip=?,maxusers=?", ipString, PreferencesHolder.getMaxUsersPerIp());
                return new IpAddress(ipString, PreferencesHolder.getMaxUsersPerIp(), true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save(IpAddress toSave) {
        if(toSave == null) {
            return;
        }

        PreferencesHolder.sql.safelyExecuteUpdate("UPDATE mt_main.xlogin_ips SET maxusers=?,sessions_on=? WHERE ip=?",
                toSave.getMaxUsers(), toSave.isSessionsEnabled(), toSave.getIp());
        cache.put(toSave.getIp(), toSave);
    }

    public static boolean exists(String ipString) {
        try (QueryResult qr = PreferencesHolder.sql.executeQueryWithResult("SELECT COUNT(*) AS cnt FROM " + IpAddress.TABLE_NAME + " " +
                "WHERE ip=?", ipString).assertHasResultSet()) {
            return qr.rs().next() && qr.rs().getInt("cnt") > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeFromCache(String ipString) {
        cache.remove(ipString);
    }

    public static void clear() {
        cache.clear();
    }
}
