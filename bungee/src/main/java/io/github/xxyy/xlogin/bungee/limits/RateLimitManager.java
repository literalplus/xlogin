package io.github.xxyy.xlogin.bungee.limits;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.lang3.ArrayUtils;

import io.github.xxyy.xlogin.bungee.XLoginPlugin;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages join rate limits in order to protect from DoS attacks.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 07/10/15
 */
public class RateLimitManager {
    public static final int JOIN_LIMIT_RESET_INTERVAL = 30;
    public static final int IP_JOIN_THRESHOLD = 5;
    private final SimpleRateLimit joinLimit = new SimpleRateLimit(this,
            "[POSSIBLE ATTACK] %d players tried to join in " + JOIN_LIMIT_RESET_INTERVAL + "s!",
            30);
    private final SimpleRateLimit registerLimit = new SimpleRateLimit(this,
            "[POSSIBLE ATTACK] %d players tried to register in " + JOIN_LIMIT_RESET_INTERVAL + "s!",
            5);
    private Map<String, Integer> ipJoins = new ConcurrentHashMap<>(); //Gets reset automatically every x seconds
    private final XLoginPlugin plugin;
    private boolean started = false;

    public RateLimitManager(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Preconditions.checkState(!started, "Rate limit manager already started!");
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                joinLimit.reset();
                registerLimit.reset();
                Map<String, Integer> newIpJoins = new ConcurrentHashMap<>(); //keep overly infringing ips
                for (Map.Entry<String, Integer> entry : ipJoins.entrySet()) {
                    if (entry.getValue() > IP_JOIN_THRESHOLD){
                        sendNotice("[POSSIBLE ATTACK] %d players tried to join from %s in %d!",
                                entry.getValue(), entry.getKey(), JOIN_LIMIT_RESET_INTERVAL);
                        newIpJoins.put(entry.getKey(), entry.getValue() / 2);
                    }
                }
                ipJoins = newIpJoins;
            }
        }, JOIN_LIMIT_RESET_INTERVAL, JOIN_LIMIT_RESET_INTERVAL, TimeUnit.SECONDS);
        started = true;
    }

    /**
     * Checks if a new connection is limited currently by the global rate limit. Note that this registers a new
     * connection for the limit.
     *
     * @return whether a new connection should be blocked
     */
    public boolean checkGlobalLimit() {
        return joinLimit.incrementAndCheck();
    }

    /**
     * Checks if a new connection is limited currently be the rate limit per IP address. Note that this registers a new
     * connection for that address' limit.
     *
     * @param address the address of the new connection
     * @return whether the new connection should be blocked
     */
    public boolean checkIpLimit(InetSocketAddress address) {
        String ipString = address.getAddress().toString();
        Integer currentValue = ipJoins.get(ipString);
        currentValue = (currentValue == null ? 0 : currentValue) + 1;
        ipJoins.put(ipString, currentValue);
        return currentValue > IP_JOIN_THRESHOLD;
    }

    /**
     * Checks if a new connection is blocked by any of this manager's limits. Note that this does register the new
     * connection with the limits.
     *
     * @param address the address of the new connection
     * @return whether the new connection should be blocked
     */
    public boolean checkLimited(InetSocketAddress address) {
        return checkGlobalLimit() || checkIpLimit(address);
    }

    /**
     * Notifies logger and permitted players of an important event related to rate limiting.
     *
     * @param message   the message, using {@link String#format(String, Object...)} format for replacing arguments
     * @param arguments the arguments for the message
     */
    public void sendNotice(String message, Object... arguments) {
        String mergedMessage = String.format(message, arguments);
        plugin.getLogger().warning(mergedMessage);
        BaseComponent[] jsonMessage = ArrayUtils.addAll(
                plugin.getMessages().jsonPrefix,
                new TextComponent(mergedMessage)
        );
        for (ProxiedPlayer plr : plugin.getProxy().getPlayers()) {
            if (plr.hasPermission("xlogin.admin")){
                plr.sendMessage(jsonMessage);
            }
        }
    }

    public SimpleRateLimit getJoinLimit() {
        return joinLimit;
    }

    public SimpleRateLimit getRegisterLimit() {
        return registerLimit;
    }
}
