/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee.limits;

import com.google.common.base.Preconditions;
import li.l1t.xlogin.bungee.XLoginPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.lang3.ArrayUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages join rate limits in order to protect from DoS attacks.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 07/10/15
 */
public class RateLimitManager {
    public static final int LIMIT_RESET_INTERVAL = 30;
    public static final int IP_JOIN_THRESHOLD = 3;
    private final AdaptiveRateLimit joinLimit = new AdaptiveRateLimit(this,
            "[/xlol] %d/%d players tried to join in " + LIMIT_RESET_INTERVAL + "s!",
            30, 0.75F, 10);
    private final SimpleRateLimit registerLimit = new SimpleRateLimit(this,
            "[/xlol] %d/%d players tried to register in " + LIMIT_RESET_INTERVAL + "s!",
            5);
    private final Map<String, IpRateLimit> ipLimits = new ConcurrentHashMap<>();
    private final Set<UUID> noticeIgnorers = new HashSet<>();
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
                StringBuilder sb = new StringBuilder();
                for (IpRateLimit limit : ipLimits.values()) {
                    if (limit.isSuspicious()) {
                        sb.append(String.format("%d from %s, ", limit.getCurrentValue(), limit.getIpString()));
                    }
                    limit.reset();
                }
                if (sb.length() > 0) {
                    sendNotice("[/xlol] Suspicious player join counts: %s in %d seconds!",
                            sb.toString(), LIMIT_RESET_INTERVAL);
                }
            }
        }, LIMIT_RESET_INTERVAL, LIMIT_RESET_INTERVAL, TimeUnit.SECONDS);
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
        return getOrCreateLimit(address).incrementAndCheck();
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
     * Notifies logger and permitted players of an important event related to rate limiting. If null is passed as a
     * message, no notice is issued.
     *
     * @param message   the message, using {@link String#format(String, Object...)} format for replacing arguments
     * @param arguments the arguments for the message
     */
    public void sendNotice(String message, Object... arguments) {
        if (message == null) {
            return;
        }
        String mergedMessage = String.format(message, arguments);
        plugin.getLogger().warning(mergedMessage);
        BaseComponent[] jsonMessage = ArrayUtils.addAll(
                plugin.getMessages().jsonPrefix,
                new TextComponent(mergedMessage)
        );
        for (ProxiedPlayer plr : plugin.getProxy().getPlayers()) {
            if (plr.hasPermission("xlogin.admin") && !doesIgnoreNotices(plr.getUniqueId())) {
                plr.sendMessage(jsonMessage);
            }
        }
    }

    /**
     * Resets all limits managed by this manager to their initial state, as if the server was restarted.
     */
    public void resetAllLimits() {
        joinLimit.reset();
        joinLimit.resetThreshold();
        registerLimit.reset();
        for (IpRateLimit limit : ipLimits.values()) {
            limit.reset();
            limit.resetTimeLimit();
        }
    }

    /**
     * Blocks an IP address from joining the server for a specified amount of time. Note that this is lost if the server
     * is restarted.
     *
     * @param address the address to block
     * @param amount  how long to block them
     * @param unit    the unit of {@code amount}
     */
    public void blockIpFor(InetSocketAddress address, int amount, TimeUnit unit) {
        //how many users would have to join for the ip to be limited for that time
        long blockedForHits = unit.toSeconds(amount) / LIMIT_RESET_INTERVAL;
        int blockedForInt = blockedForHits > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) blockedForHits;
        getOrCreateLimit(address).forceLimitFor(blockedForInt);
    }

    public Collection<IpRateLimit> getIpRateLimits() {
        return ipLimits.values();
    }

    public SimpleRateLimit getJoinLimit() {
        return joinLimit;
    }

    public SimpleRateLimit getRegisterLimit() {
        return registerLimit;
    }

    public IpRateLimit getOrCreateLimit(InetSocketAddress address) {
        String ipString = address.getAddress().toString();
        IpRateLimit limit = ipLimits.get(ipString);
        if (limit == null) {
            limit = new IpRateLimit(this, ipString, IP_JOIN_THRESHOLD);
            ipLimits.put(ipString, limit);
        }
        return limit;
    }

    /**
     * Toggles whether a player ignores notices sent by the rate limit manager.
     *
     * @param uuid the unique id of the player
     * @return whether notices are ignored by that player now
     */
    public boolean toggleIgnoresNotices(UUID uuid) {
        if (noticeIgnorers.contains(uuid)) {
            noticeIgnorers.remove(uuid);
        } else {
            noticeIgnorers.add(uuid);
        }
        return doesIgnoreNotices(uuid);
    }

    /**
     * Checks whether a player ignores notices sent by the rate limit manager.
     *
     * @param uuid the unique id of the player
     * @return whether notices are ignored by that player
     */
    public boolean doesIgnoreNotices(UUID uuid) {
        return noticeIgnorers.contains(uuid);
    }
}
