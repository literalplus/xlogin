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

import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.ips.IpAddress;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages online player rate limits per ip address.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 07/10/15
 */
public class IpOnlineLimitManager {
    private final XLoginPlugin plugin;
    private Map<String, Integer> ipOnlinePlayers = new ConcurrentHashMap<>();

    public IpOnlineLimitManager(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a player from given IP address should be allowed to connect based on the amount of players already
     * online from that address. Does not register the new connection with the limit.
     *
     * @param address the address the player is trying to connect with
     * @return whether the new connection should be blocked
     */
    public boolean checkOnlineLimit(InetSocketAddress address) {
        String ipString = address.getAddress().toString();
        IpAddress ipAddress = IpAddress.fromIpString(ipString);
        int maxUsers = ipAddress == null ? plugin.getConfig().getMaxUsers() : ipAddress.getMaxUsers();
        Integer onlinePlayers = ipOnlinePlayers.get(ipString);
        return onlinePlayers != null && onlinePlayers >= maxUsers;
    }

    public void registerOnlinePlayer(InetSocketAddress address) {
        String ipString = address.getAddress().toString();
        Integer onlinePlayers = ipOnlinePlayers.get(ipString);
        onlinePlayers = onlinePlayers == null ? 0 : onlinePlayers;
        ipOnlinePlayers.put(ipString, onlinePlayers + 1);
    }

    public void recomputeOnlinePlayers(InetSocketAddress address) {
        int onlineAmount = 0;
        String ipString = address.getAddress().toString();
        for (ProxiedPlayer plr : plugin.getProxy().getPlayers()) {
            if (plr.getAddress().equals(address)) {
                onlineAmount++;
            }
        }
        if (onlineAmount > 0) {
            ipOnlinePlayers.put(ipString, onlineAmount);
        } else {
            ipOnlinePlayers.remove(ipString);
        }
    }

    public void recomputeOnlinePlayers() {
        ipOnlinePlayers.clear();
        Map<InetSocketAddress, Integer> addressCounts = new HashMap<>(plugin.getProxy().getPlayers().size());
        for (ProxiedPlayer plr : plugin.getProxy().getPlayers()) {
            Integer currentCount = addressCounts.get(plr.getAddress());
            if (currentCount == null) {
                addressCounts.put(plr.getAddress(), 1);
            } else {
                addressCounts.put(plr.getAddress(), currentCount + 1);
            }
        }

        for (Map.Entry<InetSocketAddress, Integer> entry : addressCounts.entrySet()) {
            ipOnlinePlayers.put(entry.getKey().getAddress().toString(), entry.getValue());
        }
    }
}
