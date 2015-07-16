package io.github.xxyy.xlogin.bungee.config;

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

import io.github.xxyy.lib.intellij_annotations.NotNull;

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
    @NotNull
    private final transient Plugin plugin;
    @NotNull
    public String prefix = "§6[§8xLogin§6] §7";
    public transient BaseComponent[] jsonPrefix;
    @NotNull
    public String alreadyLoggedIn = "Du bist bereits eingeloggt!";
    @NotNull
    public String notLoggedIn = "Du bist nicht eingeloggt! §6/login";

    @NotNull
    public String notRegistered = "Du bist nicht registriert! Versuche §8/register§7!";
    @NotNull
    public String alreadyRegistered = "Du bist bereits registriert! Versuche §8/login <Passwort>§7!";

    @NotNull
    @Comment("param1: Name of the user who just registered")
    public String welcome = "Willkommen auf ✪MinoTopia✪, {0}!";

    @NotNull
    public String commandLoginUsage = "§eVerwendung: /login <Passwort> §7Loggt dich ein.";
    @NotNull
    public String commandRegisterUsage = "§eVerwendung: /register <Passwort> <Passwort> §7Registriert einen Account.";
    @NotNull
    public String commandSessionsUsage = "§eVerwendung: /sessions [on|off] §7Schaltet Sitzungen ein oder aus.";
    @NotNull
    public String commandPremiumUsage = "§eVerwendung: /premium §7Markiert deinen Account als bei minecraft.net gekauft.";
    @NotNull
    public String commandCpwUsage = "§eVerwendung: /cpw <Altes Passwort> <Neues Passwort> <Neues Passwort>";

    @NotNull
    public String sessionsEnabled = "Sitzungen sind aktiviert.";
    @NotNull
    public String sessionsDisabled = "Sitzungen sind deaktiviert.";
    @NotNull
    public String sessionsUnavailable = "Sitzungen sind momentan global deaktiviert. Bitte entschuldige die Unannehmlichkeiten!";
    @NotNull
    public String sessionsPremium = "Als Minecraft-Premium-Benutzer brauchst du keine Sessions! :)";

    @NotNull
    public String premiumLoggedIn = "§6[§a/premium§6] §7Du wurdest automatisch eingeloggt!";
    @NotNull
    public String sessionsLoggedIn = "§6[§a/sessions§6] §7Deine Sitzung ist noch gültig.";
    @NotNull
    public String successfullyAuthenticated = "Du hast dich erfolgreich eingeloggt!";
    @NotNull
    public String premiumAvailable = "Hast du diesen Account bei minecraft.net gekauft? Verwende §8/premium §7, um dich nicht mehr einloggen zu müssen!";

    @NotNull
    public String accountNotPremium = "Entschuldigung, aber dein Account schaut nicht nach einem Minecraft Premium-Account aus.";
    @NotNull
    public String accountAlreadyPremium = "Dein Account ist bereits als Premium markiert! =)";
    @NotNull
    public String accountMarkedPremium = "Dein Account wurde als Premium markiert! Probiere es gleich aus! ;)";
    @NotNull
    public String premiumWarning = "§cACHTUNG! Wenn du fortfährst, kannst du dich nicht mehr gecrackt einloggen! " +
            "Es werden vermutlich deinen Spielerdaten wie Inventare, Ränge, etc verloren gehen, also" +
            "überlege diesen Schritt genau und spreche dies mit dem Team ab, wenn du deine Sachen" +
            "behalten möchtest. " +
            "Wenn die Sessionserver nicht verfügbar sind, wirst du auch nicht reinkommen." +
            " Bitte überlege diesen Schritt genau und f&uuml;hre ihn wirklich NUR durch," +
            " wenn DU &ldiesen Account bei &lminecraft.net gekauft hast! §cDu wurdest gewarnt!\n" +
            "    §4Fortfahren (auf eigene Gefahr): &l/premium sicher ";

    @NotNull
    @Comment("param1: IP address the user joined with param2: max users allowed for that IP")
    public String ipAccountLimitedReached = "Für deine IP {0} gibt es bereits mehr als {1} Accounts!";
    @NotNull
    public String nameBlocked = "Diesen Namen kannst du leider nicht verwenden.";

    @NotNull
    public String passwordsDontMatch = "Die Passwörter stimmen nicht überein!";
    @NotNull
    public String passwordTooShort = "Bitte verwende ein Passwort mit mehr als 4 Zeichen!";
    @NotNull
    public String passwordInsecure = "Bitte verwende ein sichereres Passwort! :)";

    @NotNull
    public String passwordChangeAdmin = "Dein Passwort wurde von {0} geändert. Bitte kontaktiere diese Person.";
    @NotNull
    public String premiumAdmin = "{0} hat deinen Account als Premium markiert!";

    @NotNull
    public String wrongPassword = "Falsches Passwort! :(";
    @NotNull
    public String sessionsEngaged = "Du musst dich für eine Zeit nicht mehr einloggen, da du Sitzungen aktiviert hast! Verwende §6/sessions off§f, um dies abzuschalten.";

    @NotNull
    public String oldPasswordIncorrect = "Dein altes Passwort ist inkorrekt!";
    @NotNull
    public String passwordChanged = "Passwort geändert!";

    @NotNull
    @Comment("Is shown before list of failed login attempts after login. param1: amount of failed attempts")
    public String failedLoginAttemptsFound = "§eEs gab §6{0} §egescheiterte Loginversuche auf deinen Account:";

    @NotNull
    @Comment("param0: Date object. Non-JSON example: {0,time,dd.MM.yyyy HH:mm}//param1: IP address")
    public String failedLoginAttemptItem = "§eam §6{0,time,dd.MM.yyyy HH:mm} §evon §6{1}§e ";

    @NotNull
    @Comments({"This will be prepepended with the prefix and clickable player name",
            "param0: Amount of warns dealt"})
    @Path("warn-broadcast-header")
    public String warnBroadcastHeader = "§c wurde {0}mal gewarnt. Grund:";
    @NotNull
    @Comment("param0: Warn reason")
    @Path("warn-broadcast-body")
    public String warnBroadcastBody = "§c=>§6{0}§c<=";

    @NotNull
    @Comment("param0: target name param1: string describing the ban time, with formatting codes")
    public String banBroadcastHeader = "§6{0}§c wurde {1} §cgebannt! Grund:";
    @NotNull
    public String banBroadcastBody = "§c=>§6{0}§c<=";

    public LocalisedMessageConfig(@NotNull Plugin plugin) {
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

    public BaseComponent[] parseMessage(@NotNull String rawMessage) {
        if (rawMessage.startsWith("!JSON")) {
            return parseMessage(rawMessage, true);
        } else {
            return parseMessage(rawMessage, false);
        }
    }

    private BaseComponent[] parseMessage(@NotNull String rawMessage, boolean isJSON) {
        if (isJSON) {
            return ComponentSerializer.parse(rawMessage);
        } else {
            return TextComponent.fromLegacyText(rawMessage);
        }
    }

    public void sendMessageWithPrefix(@NotNull String rawMessage, @NotNull CommandSender sender) {
        sender.sendMessage(parseMessageWithPrefix(rawMessage));
    }

    public void sendMessage(@NotNull String rawMessage, @NotNull CommandSender sender, Object... args) {
        sender.sendMessage(parseMessage(rawMessage, args));
    }

    public BaseComponent[] parseMessageWithPrefix(@NotNull String rawMessage) {
        if (rawMessage.startsWith("!JSON")) {
            return parseMessageWithPrefix(rawMessage, true);
        } else {
            return parseMessageWithPrefix(rawMessage, false);
        }
    }

    private BaseComponent[] parseMessageWithPrefix(@NotNull String rawMessage, boolean isJSON) {
        return ArrayUtils.addAll(jsonPrefix, parseMessage(rawMessage, isJSON));
    }

    public BaseComponent[] parseMessageWithPrefix(@NotNull String rawMessage, Object... args) {
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

    public BaseComponent[] parseMessage(@NotNull String rawMessage, Object... args) {
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
