package io.github.xxyy.xlogin.bungee.limits;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.ips.IpAddress;

import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Manages registered account limit per ip address.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 07/10/15
 */
public class IpAccountLimitManager {
    private final XLoginPlugin plugin;
    private Map<String, Integer> ipOnlinePlayers = new ConcurrentHashMap<>();
    private Map<ProxiedPlayer, Future<Boolean>> futuresById = new WeakHashMap<>();

    public IpAccountLimitManager(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks whether a player may connect to the server based on the amount of accounts registered for their ip address.
     * Because the computation requires access to database resources, it it performed in a separate thread and the result
     * provided by a {@link Future}. The future can later be obtained with {@link #getAccountLimitFuture(ProxiedPlayer)}.
     *
     * @param player the player attempting to connect to the server
     * @return whether the player should be allowed to connect and possibly create an account
     */
    public Future<Boolean> requestAccountLimit(final ProxiedPlayer player) {
        final FutureTask<Boolean> future = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String ipString = player.getAddress().getAddress().toString();
                IpAddress ipAddress = IpAddress.fromIpString(ipString);
                Integer maxUsers = getMaxUsers(ipAddress);
                int registeredCount = getRegisteredCount(ipString, player);
                return registeredCount >= maxUsers;
            }
        });
        plugin.getProxy().getScheduler().runAsync(plugin, future);
        futuresById.put(player, future);
        return future;
    }

    /**
     * Gets the maximum allowed users for given ip or the default if null is passed
     *
     * @param ipAddress the ip address to check or null for the global default
     * @return the maxium amount of users allowed for the address denoted by the parameter
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
        return futuresById.remove(player);
    }

    private int getRegisteredCount(String ipString, ProxiedPlayer ignore) throws SQLException {
        int registeredCount = 0;
        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT COUNT(*) AS cnt FROM " +
                        AuthedPlayer.AUTH_DATA_TABLE_NAME +
                        "WHERE user_lastip=? AND uuid != ? AND username != ?",
                ipString, ignore.getUniqueId().toString(), ignore.getName())) {
            if (qr.rs().next()) {
                registeredCount = qr.rs().getInt("cnt");
            }
        }
        return registeredCount;
    }
}
