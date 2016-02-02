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

import com.google.common.base.Preconditions;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.ips.IpAddress;
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
