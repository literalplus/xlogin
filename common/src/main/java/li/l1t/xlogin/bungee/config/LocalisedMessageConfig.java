/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.Path;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Represents the configuration file used to
 * store customised messages.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 21.5.14
 */
public class LocalisedMessageConfig extends Config { //FIXME This impl is shitty
    @Nonnull
    private final transient Plugin plugin;
    @Nonnull
    public String prefix = "§6[§8xLogin§6] §7";
    public transient BaseComponent[] jsonPrefix;
    @Nonnull
    public String alreadyLoggedIn = "Du bist bereits eingeloggt!";
    @Nonnull
    public String notLoggedIn = "Du bist nicht eingeloggt! §6/login";

    @Nonnull
    public String notRegistered = "Du bist nicht registriert! Versuche §8/register§7!";
    @Nonnull
    public String alreadyRegistered = "Du bist bereits registriert! Versuche §8/login <Passwort>§7!";
    @Nonnull
    public String registerRateLimit = "Es registrieren sich gerade zu viele Leute. Bitte versuche es in Kürze erneut!";

    @Nonnull
    @Comment("param1: Name of the user who just registered")
    public String welcome = "Willkommen auf ✪MinoTopia✪, {0}!";

    @Nonnull
    public String commandLoginUsage = "§eVerwendung: /login <Passwort> §7Loggt dich ein.";
    @Nonnull
    public String commandRegisterUsage = "§eVerwendung: /register <Passwort> <Passwort> §7Registriert einen Account.";
    @Nonnull
    public String commandSessionsUsage = "§eVerwendung: /sessions [on|off] §7Schaltet Sitzungen ein oder aus.";
    @Nonnull
    public String commandPremiumUsage = "§eVerwendung: /premium §7Markiert deinen Account als bei minecraft.net gekauft.";
    @Nonnull
    public String commandCpwUsage = "§eVerwendung: /cpw <Altes Passwort> <Neues Passwort> <Neues Passwort>";

    @Nonnull
    public String sessionsEnabled = "Sitzungen sind aktiviert.";
    @Nonnull
    public String sessionsDisabled = "Sitzungen sind deaktiviert.";
    @Nonnull
    public String sessionsUnavailable = "Sitzungen sind momentan global deaktiviert. Bitte entschuldige die Unannehmlichkeiten!";
    @Nonnull
    public String sessionsPremium = "Als Minecraft-Premium-Benutzer brauchst du keine Sessions! :)";

    @Nonnull
    public String premiumLoggedIn = "§6[§a/premium§6] §7Du wurdest automatisch eingeloggt!";
    @Nonnull
    public String sessionsLoggedIn = "§6[§a/sessions§6] §7Deine Sitzung ist noch gültig.";
    @Nonnull
    public String successfullyAuthenticated = "Du hast dich erfolgreich eingeloggt!";
    @Nonnull
    public String premiumAvailable = "Hast du diesen Account bei minecraft.net gekauft? Verwende §8/premium §7, um dich nicht mehr einloggen zu müssen!";

    @Nonnull
    public String accountNotPremium = "Entschuldigung, aber dein Account schaut nicht nach einem Minecraft Premium-Account aus.";
    @Nonnull
    public String accountAlreadyPremium = "Dein Account ist bereits als Premium markiert! =)";
    @Nonnull
    public String accountMarkedPremium = "Dein Account wurde als Premium markiert! Probiere es gleich aus! ;)";
    @Nonnull
    public String premiumWarning = "§cACHTUNG! Wenn du fortfährst, kannst du dich nicht mehr gecrackt einloggen! " +
            "Es werden vermutlich deinen Spielerdaten wie Inventare, Ränge, etc verloren gehen, also" +
            "überlege diesen Schritt genau und spreche dies mit dem Team ab, wenn du deine Sachen" +
            "behalten möchtest. " +
            "Wenn die Sessionserver nicht verfügbar sind, wirst du auch nicht reinkommen." +
            " Bitte überlege diesen Schritt genau und f&uuml;hre ihn wirklich NUR durch," +
            " wenn DU &ldiesen Account bei &lminecraft.net gekauft hast! §cDu wurdest gewarnt!\n" +
            "    §4Fortfahren (auf eigene Gefahr): &l/premium sicher ";

    @Nonnull
    @Comment("param1: IP address the user joined with param2: max users allowed for that IP")
    public String ipAccountLimitedReached = "Für deine IP {0} gibt es bereits mehr als {1} Accounts!";
    @Nonnull
    @Comment("param1: IP address the user joined with")
    public String ipOnlineLimitReached = "§c[xLogin] Mit deiner IP {0} sind schon zu viele Spieler online!";
    @Nonnull
    public String nameBlocked = "Diesen Namen kannst du leider nicht verwenden.";

    @Nonnull
    public String passwordsDontMatch = "Die Passwörter stimmen nicht überein!";
    @Nonnull
    public String passwordTooShort = "Bitte verwende ein Passwort mit mehr als 4 Zeichen!";
    @Nonnull
    public String passwordInsecure = "Bitte verwende ein sichereres Passwort! :)";

    @Nonnull
    public String passwordChangeAdmin = "Dein Passwort wurde von {0} geändert. Bitte kontaktiere diese Person.";
    @Nonnull
    public String premiumAdmin = "{0} hat deinen Account als Premium markiert!";

    @Nonnull
    public String wrongPassword = "Falsches Passwort! :(";
    @Nonnull
    public String sessionsEngaged = "Du musst dich für eine Zeit nicht mehr einloggen, da du Sitzungen aktiviert hast! Verwende §6/sessions off§f, um dies abzuschalten.";

    @Nonnull
    public String oldPasswordIncorrect = "Dein altes Passwort ist inkorrekt!";
    @Nonnull
    public String passwordChanged = "Passwort geändert!";

    @Nonnull
    @Comment("Is shown before list of failed login attempts after login. param1: amount of failed attempts")
    public String failedLoginAttemptsFound = "§eEs gab §6{0} §egescheiterte Loginversuche auf deinen Account:";

    @Nonnull
    @Comment("param0: Date object. Non-JSON example: {0,time,dd.MM.yyyy HH:mm}//param1: IP address")
    public String failedLoginAttemptItem = "§eam §6{0,time,dd.MM.yyyy HH:mm} §evon §6{1}§e ";

    @Nonnull
    @Comments({"This will be prepepended with the prefix and clickable player name",
            "param0: Amount of warns dealt"})
    @Path("warn-broadcast-header")
    public String warnBroadcastHeader = "§c wurde {0}mal gewarnt. Grund:";
    @Nonnull
    @Comment("param0: Warn reason")
    @Path("warn-broadcast-body")
    public String warnBroadcastBody = "§c=>§6{0}§c<=";

    @Nonnull
    @Comment("param0: target name param1: string describing the ban time, with formatting codes")
    public String banBroadcastHeader = "§6{0}§c wurde {1} §cgebannt! Grund:";
    @Nonnull
    public String banBroadcastBody = "§c=>§6{0}§c<=";

    public LocalisedMessageConfig(@Nonnull Plugin plugin) {
        CONFIG_HEADER = new String[]{"Localised message configuration file for xLogin, BungeeCord edition.",
                "Prepend text with !JSON to use JSON formatting.",
                "For arguments, use {n} with non-JSON chat, where n is the index of the argument, starting with 0.",
                "For JSON chat, use %s or %n$s for numbered argument, where n is the number of the argument, starting with 1.",
                "For non-JSON chat, you can use § formatting codes. (& won't work!)",
                "Make sure that you know what you're doing before changing anything. Thank you!",
                "xLogin is not free software and may not be used without explicit written permission",
                "from the author, which you can contact at devnull@nowak-at.net."};
        CONFIG_FILE = new File(plugin.getDataFolder(), "messages.yml");

        this.plugin = plugin;

        jsonPrefix = TextComponent.fromLegacyText(prefix); //TODO reload lang
    }

    public BaseComponent[] parseMessage(@Nonnull String rawMessage) {
        if (rawMessage.startsWith("!JSON")) {
            return parseMessage(rawMessage, true);
        } else {
            return parseMessage(rawMessage, false);
        }
    }

    private BaseComponent[] parseMessage(@Nonnull String rawMessage, boolean isJSON) {
        if (isJSON) {
            return ComponentSerializer.parse(rawMessage);
        } else {
            return TextComponent.fromLegacyText(rawMessage);
        }
    }

    public void sendMessageWithPrefix(@Nonnull String rawMessage, @Nonnull CommandSender sender) {
        sender.sendMessage(parseMessageWithPrefix(rawMessage));
    }

    public void sendMessage(@Nonnull String rawMessage, @Nonnull CommandSender sender, Object... args) {
        sender.sendMessage(parseMessage(rawMessage, args));
    }

    public BaseComponent[] parseMessageWithPrefix(@Nonnull String rawMessage) {
        if (rawMessage.startsWith("!JSON")) {
            return parseMessageWithPrefix(rawMessage, true);
        } else {
            return parseMessageWithPrefix(rawMessage, false);
        }
    }

    private BaseComponent[] parseMessageWithPrefix(@Nonnull String rawMessage, boolean isJSON) {
        return ArrayUtils.addAll(jsonPrefix, parseMessage(rawMessage, isJSON));
    }

    public BaseComponent[] parseMessageWithPrefix(@Nonnull String rawMessage, Object... args) {
        try {
            if (rawMessage.startsWith("!JSON")) {
                return parseMessageWithPrefix(String.format(rawMessage, args), true);
            } else {
                return parseMessageWithPrefix(MessageFormat.format(rawMessage, args), false);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(MessageFormat.format("Illegal format: {0} for args: {1}", rawMessage, Arrays.toString(args)));
            return parseMessage(rawMessage);
        }
    }

    public BaseComponent[] parseMessage(@Nonnull String rawMessage, Object... args) {
        try {
            if (rawMessage.startsWith("!JSON")) {
                return parseMessage(String.format(rawMessage, args), true);
            } else {
                return parseMessage(MessageFormat.format(rawMessage, args), false);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(MessageFormat.format("Illegal format: {0} for args: {1}", rawMessage, Arrays.toString(args)));
            return parseMessage(rawMessage);
        }
    }
}
