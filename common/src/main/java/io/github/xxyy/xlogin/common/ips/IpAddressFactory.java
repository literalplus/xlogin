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
        if(cache.containsKey(ipString)) {
            return cache.get(ipString);
        }

        try (QueryResult qr = PreferencesHolder.sql.executeQueryWithResult("SELECT * FROM mt_main.xlogin_ips WHERE ip=?", ipString).assertHasResultSet()) {
            ResultSet rs = qr.rs();

            if(rs.next()) {
                IpAddress ipAddress = new IpAddress(ipString, rs.getInt("maxusers"), rs.getBoolean("sessions_on"));
                cache.put(ipString, ipAddress);
                return ipAddress;
            } else {
                PreferencesHolder.sql.safelyExecuteUpdate("INSERT INTO mt_main.xlogin_ips SET ip=?,maxusers=?", ipString, PreferencesHolder.getMaxUsersPerIp());
                return new IpAddress(ipString, PreferencesHolder.getMaxUsersPerIp(), true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        cache.clear();
    }
}
