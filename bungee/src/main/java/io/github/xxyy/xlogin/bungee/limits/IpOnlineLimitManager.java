/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.limits;

import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.ips.IpAddress;
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
