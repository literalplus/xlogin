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

package li.l1t.xlogin.common.ips;

import li.l1t.common.sql.QueryResult;
import li.l1t.xlogin.common.PreferencesHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    public static void free(@Nonnull String ipString, int newSlotAmount) {
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
