package io.github.xxyy.xlogin.bungee.config;

import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import lombok.Getter;
import lombok.Setter;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.Path;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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

    @Comments({"A list of passwords which are considered unsafe and therefore will be prohibited from being used.",
            "Examples are: password, 1234"})
    @Path("unsafe-passwords")
    private List<String> unsafePasswords = Arrays.asList("password", "1234", "passwort");

    @Comments({"A list of names which will be prohibited from being used. Any name that contains any of these is blocked from joining.",
            "Examples are: chris301234, JoinHiveMc"})
    @Path("blocked-names")
    private List<String> blockedNames = Arrays.asList("chris301234", "hivemc", "kanney");

    @Comment("Session expiry time, in seconds. Default is 43200, which is 12 hours.")
    @Path("session-expriy-time")
    private int sessionExpiryTime = 43_200;

    @Comment("Maximum amount of users per IP address. Default is 1.")
    @Path("max-users-per-ip")
    private int maxUsers = 1;

    public XLoginConfig(XLoginPlugin plugin) {
        CONFIG_HEADER = new String[]{"Main configuration file for xLogin, BungeeCord edition.",
                "Make sure that you know what you're doing before changing anything. Thank you!",
                "xLogin is not free software and may not be used without explicit written permission",
                "from the author, which you can contact at devnull@nowak-at.net."};
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");

        PreferencesHolder.setMaxUsersPerIp(maxUsers);
        PreferencesHolder.setSessionExpriyTime(sessionExpiryTime);
    }
}
