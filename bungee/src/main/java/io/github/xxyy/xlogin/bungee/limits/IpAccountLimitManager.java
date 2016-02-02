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

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.ips.IpAddress;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.*;

/**
 * Manages registered account limit per ip address.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 07/10/15
 */
public class IpAccountLimitManager {
    private final XLoginPlugin plugin;
    private Map<String, Integer> ipOnlinePlayers = new ConcurrentHashMap<>();
    private Map<UUID, Future<Boolean>> futuresById = new WeakHashMap<>();

    public IpAccountLimitManager(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks whether a player may connect to the server based on the amount of accounts registered for their ip address.
     * Because the computation requires access to database resources, it it performed in a separate thread and the result
     * provided by a {@link Future}. The future can later be obtained with {@link #getAccountLimitFuture(ProxiedPlayer)}.
     *
     * @param uuid    the unique id of the player attempting to connect
     * @param name    the name of the player
     * @param address the address the player is using to connect
     * @return whether the player should be allowed to connect and possibly create an account
     */
    public Future<Boolean> requestAccountLimit(final UUID uuid, final String name, final InetSocketAddress address) {
        final FutureTask<Boolean> future = createFuture(uuid, name, address);
        plugin.getProxy().getScheduler().runAsync(plugin, future);
        futuresById.put(uuid, future);
        return future;
    }

    /**
     * Checks whether a player may connect to the server based on the amount of accounts registered for their ip address.
     * This computes the result in the current thread and returns it immediately. Note that this might take some time
     * because it requires database access.
     *
     * @param uuid    the unique id of the player attempting to connect
     * @param name    the name of the player
     * @param address the address the player is using to connect
     * @return whether the player should be allowed to connect and possibly create an account
     */
    public boolean getAccountLimit(final UUID uuid, final String name, final InetSocketAddress address) throws ExecutionException, InterruptedException {
        return createFuture(uuid, name, address).get();
    }

    @NotNull
    private FutureTask<Boolean> createFuture(final UUID uuid, final String name, final InetSocketAddress address) {
        return new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String ipString = address.getAddress().toString();
                IpAddress ipAddress = IpAddress.fromIpString(ipString);
                Integer maxUsers = getMaxUsers(ipAddress);
                int registeredCount = getRegisteredCount(ipString, uuid, name);
                return registeredCount >= maxUsers;
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

    /**
     * Gets and removes the future for the account limit computation for a specified player. If no computation has
     * been requested, null is returned.
     *
     * @param player the player to request the future for
     * @return the future or null if no computation has been requested
     */
    public Future<Boolean> getAccountLimitFuture(ProxiedPlayer player) {
        return futuresById.remove(player.getUniqueId());
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
