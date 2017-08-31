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

package li.l1t.xlogin.bungee.config;

import li.l1t.xlogin.common.PreferencesHolder;
import lombok.Getter;
import lombok.Setter;
import net.cubespace.Yamler.Config.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.annotation.Nonnull;
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

    @Nonnull
    @Comments({"A list of passwords which are considered unsafe and therefore will be prohibited from being used.",
            "Examples are: password, 1234"})
    @Path("unsafe-passwords")
    private List<String> unsafePasswords = Arrays.asList("password", "1234", "passwort");

    @Nonnull
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

    @Nonnull
    @Comment("Enable specific modules here:")
    private Map<String, Boolean> enabledModules = new HashMap<>();

    @Comment("Whether to allow users to skip logging in when they connect using the same IP again. (Can be turned off at a per-user basis too)")
    private boolean enableSessions = true;

    @Nonnull
    @Path("dynlist")
    @Comment("Entries of the dynamic whitelist. Format: name/regex (e.g. tm/tm[1-9])")
    private List<String> dynlistEntries = new ArrayList<>();

    @Nonnull
    @Path("statsd.hostname")
    @Comment("The host name of the StatsD server to connect to. Set to 'disable' to disable sending metrics to StatsD.")
    private String statsdHost = "disable";

    @Path("statsd.port")
    @Comment("The port of the StatsD server to connect to.")
    private int statsdPort = 8125;

    @Path("statsd.gauge-update-interval")
    @Comment("Interval in seconds that static gauges are sent to StatsD (player count, ...)")
    private int gaugeUpdateInterval = 30;

    @Nonnull
    @Path("statsd.prefix")
    @Comment("Proxy-specific metric prefix for StatsD, so that multiple proxies' metrics don't interfere")
    private String statsdPrefix = "xlogin.bungee";

    public XLoginConfig(@Nonnull Plugin plugin) {
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
