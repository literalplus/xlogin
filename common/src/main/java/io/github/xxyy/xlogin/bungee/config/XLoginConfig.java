/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.config;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import lombok.Getter;
import lombok.Setter;
import net.cubespace.Yamler.Config.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.*;

/**
 * Interfaces with the Yamler configuration API.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10.5.14
 */
@Getter
@Setter
@SuppressWarnings("MismatchedReadAndWriteOfArray")
public class XLoginConfig extends Config {

    @NotNull
    @Comments({"A list of passwords which are considered unsafe and therefore will be prohibited from being used.",
            "Examples are: password, 1234"})
    @Path("unsafe-passwords")
    private List<String> unsafePasswords = Arrays.asList("password", "1234", "passwort");

    @NotNull
    @Comments({"A list of names which will be prohibited from being used. Any name that contains any of these is blocked from joining.",
            "Examples are: chris301234, JoinHiveMc"})
    @Path("blocked-names")
    private List<String> blockedNames = Arrays.asList("chris301234", "hivemc", "kanney");

    @Comment("Session expiry time, in seconds. Default is 43200, which is 12 hours.")
    @Path("session-expiry-time")
    private int sessionExpiryTime = 43_200;

    @Comment("Maximum amount of users per IP address. Default is 4.")
    @Path("max-users-per-ip")
    private int maxUsers = 4;

    @NotNull
    @Comment("Enable specific modules here:")
    private Map<String, Boolean> enabledModules = new HashMap<>();

    @Comment("Whether to allow users to skip logging in when they connect using the same IP again. (Can be turned off at a per-user basis too)")
    private boolean enableSessions = true;

    @NotNull
    @Path("dynlist")
    @Comment("Entries of the dynamic whitelist. Format: name/regex (e.g. tm/tm[1-9])")
    private List<String> dynlistEntries = new ArrayList<>();

    @NotNull
    @Path("statsd.hostname")
    @Comment("The host name of the StatsD server to connect to. Set to 'disable' to disable sending metrics to StatsD.")
    private String statsdHost = "disable";

    @Path("statsd.port")
    @Comment("The port of the StatsD server to connect to.")
    private int statsdPort = 8125;

    @Path("statsd.gauge-update-interval")
    @Comment("Interval in seconds that static gauges are sent to StatsD (player count, ...)")
    private int gaugeUpdateInterval = 30;

    @NotNull
    @Path("statsd.prefix")
    @Comment("Proxy-specific metric prefix for StatsD, so that multiple proxies' metrics don't interfere")
    private String statsdPrefix = "xlogin.bungee";

    public XLoginConfig(@NotNull Plugin plugin) {
        CONFIG_HEADER = new String[]{"Main configuration file for xLogin, BungeeCord edition.",
                "Make sure that you know what you're doing before changing anything. Thank you!",
                "xLogin is not free software and may not be used without explicit written permission",
                "from the author, which you can contact at devnull@nowak-at.net.",
                "*** WARNING *** Messages are now configured in messages.yml!"};
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
    }

    @Override
    public void init() throws InvalidConfigurationException {
        super.init();
        PreferencesHolder.setMaxUsersPerIp(maxUsers);
        PreferencesHolder.setSessionExpiryTime(sessionExpiryTime);
        ProxyServer.getInstance().getLogger().info("[xLogin] Loaded general config with maxUsers=" + maxUsers);
    }
}
