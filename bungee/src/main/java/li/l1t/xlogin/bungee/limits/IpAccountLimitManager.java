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

package li.l1t.xlogin.bungee.limits;

import com.google.common.base.Preconditions;
import li.l1t.common.sql.QueryResult;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.PreferencesHolder;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.ips.IpAddress;
import net.md_5.bungee.api.Callback;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Manages registered account limit per ip address.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 07/10/15
 */
public class IpAccountLimitManager {
    private final XLoginPlugin plugin;

    public IpAccountLimitManager(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks whether a player may connect to the server based on the amount of accounts registered for their ip address.
     * Because the computation requires access to database resources, it it performed in a separate thread.
     *
     * @param uuid     the unique id of the player attempting to connect
     * @param name     the name of the player
     * @param address  the address the player is using to connect
     * @param callback the callback that is called upon completion of the operation
     */
    public void requestAccountLimit(final UUID uuid, final String name, final InetSocketAddress address,
                                    final Callback<Boolean> callback) {
        Preconditions.checkNotNull(callback, "callback");
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    String ipString = address.getAddress().toString();
                    IpAddress ipAddress = IpAddress.fromIpString(ipString);
                    Integer maxUsers = getMaxUsers(ipAddress);
                    int registeredCount = getRegisteredCount(ipString, uuid, name);
                    callback.done(registeredCount >= maxUsers, null);
                } catch (Exception e) {
                    callback.done(null, e);
                }
            }
        });
    }

    /**
     * Gets the maximum allowed users for given ip or the default if null is passed
     *
     * @param ipAddress the ip address to check or null for the global default
     * @return the maximum amount of users allowed for the address denoted by the parameter
     */
    public int getMaxUsers(IpAddress ipAddress) {
        return ipAddress == null ? plugin.getConfig().getMaxUsers() : ipAddress.getMaxUsers();
    }

    private int getRegisteredCount(String ipString, UUID ignoreId, String ignoreName) throws SQLException {
        int registeredCount = 0;
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT COUNT(*) AS cnt FROM " +
                        AuthedPlayer.AUTH_DATA_TABLE_NAME +
                        " WHERE user_lastip=? AND uuid != ? AND username != ?",
                ipString, ignoreId.toString(), ignoreName)) {
            if (qr.rs().next()) {
                registeredCount = qr.rs().getInt("cnt");
            }
        }
        return registeredCount;
    }
}
