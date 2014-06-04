package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.common.lib.com.mojang.api.profiles.HttpProfileRepository;
import io.github.xxyy.common.lib.com.mojang.api.profiles.Profile;
import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.util.CommandHelper;
import io.github.xxyy.common.util.encryption.PasswordHelper;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import io.github.xxyy.xlogin.common.ips.IpAddress;
import io.github.xxyy.xlogin.common.ips.IpAddressFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.md_5.bungee.api.ChatColor.*;

/**
 * Handles administrative commands.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 25.5.14
 */
public class CommandxLogin extends Command {
    public static final HttpProfileRepository HTTP_PROFILE_REPOSITORY = new HttpProfileRepository("minecraft");
    private final XLoginPlugin plugin;
    private static final BaseComponent[][] HELP_COMPONENTS = {
            new ComponentBuilder("xLogin BungeeCord - Xtreme BungeeCord authentication system.").color(GOLD).create(),
            new ComponentBuilder("Copyright (C) 2014 xxyy98 aka Literallie - http://xxyy.github.io/").color(ChatColor.DARK_GRAY).create(),
            new ComponentBuilder("Version " + XLoginPlugin.PLUGIN_VERSION).color(ChatColor.DARK_GRAY).create(),
            new ComponentBuilder("/xlo [help|reload|cpw|premium|free] ").color(GOLD).append("Administrationsbefehl von xLogin.").color(ChatColor.GRAY).create(),
            new ComponentBuilder("/xlo reload ").color(GOLD).append("Reloads data/configs from database & disk.").color(ChatColor.GRAY).create(),
            new ComponentBuilder("/xlo cpw [Name] [New password] ").color(GOLD).append("Changes password of a cracked account.").color(ChatColor.GRAY).create(),
            new ComponentBuilder("/xlo free [/IP|%Teil des Namens|UUID|Name] ").color(GOLD).append("Adds slots to all IPs associated with asscounts that match given criteria.").color(ChatColor.GRAY).create(),
            new ComponentBuilder("/xlo user [/IP|%Teil des Namens|UUID|Name] ").color(GOLD).append("Displays information about users.").color(ChatColor.GRAY).create(),
            new ComponentBuilder("/xlo unregister [/IP|UUID|Name] [-R]").color(GOLD).append("PERMANENTLY unregisters a user. Cannot be undone. Add -R to remove multiple users.").color(ChatColor.GRAY).create(),
    };

    public CommandxLogin(XLoginPlugin plugin) {
        super("xlogin", "xlogin.admin", "xlo");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendAll(sender, HELP_COMPONENTS);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                XLoginPlugin.AUTHED_PLAYER_REPOSITORY.clear();
                IpAddressFactory.clear();
                plugin.resetIpOnlinePlayers();
                sender.sendMessage(new TextComponent("Reloaded BungeeCord-side message and general config, IPs, players and sessions."));
                return;
            case "cpw":
                if (args.length < 3) {
                    sendAll(sender, HELP_COMPONENTS);
                } else {
                    AuthedPlayer authedPlayer = AuthedPlayerFactory.get(UtilUUID.offlineUUID(args[1]), args[1]);
                    authedPlayer.setSalt(PasswordHelper.generateSalt());
                    authedPlayer.setPassword(PasswordHelper.encrypt(args[2], authedPlayer.getSalt()));
                    AuthedPlayerFactory.save(authedPlayer);

                    ProxiedPlayer player = plugin.getProxy().getPlayer(args[1]);
                    if (player != null) {
                        player.disconnect(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordChangeAdmin, sender.getName()));
                    }

                    sender.sendMessage(new ComponentBuilder("Das Passwort von ").color(GOLD)
                            .append(args[1]).color(ChatColor.YELLOW)
                            .append(" wurde geändert.").color(GOLD).create());
                }
                return;
            case "premium":
                if (args.length < 2) {
                    sendAll(sender, HELP_COMPONENTS);
                } else {
                    Profile[] profiles = HTTP_PROFILE_REPOSITORY.findProfilesByNames(args[1]);
                    if (profiles.length == 0 || profiles[0].getDemo()) {
                        sender.sendMessage(new ComponentBuilder("Für diesen Spieler wurde kein Premium-Account gefunden.").color(GOLD).create());
                        return;
                    } else if (profiles.length > 1) {
                        sender.sendMessage(new ComponentBuilder("Für diesen Namen gibt es mehrere Accounts. Das ist ein Problem.").color(ChatColor.RED).create());
                        return;
                    }

                    AuthedPlayer authedPlayer = AuthedPlayerFactory.get(UtilUUID.getFromString(profiles[0].getId()), args[1]);
                    authedPlayer.setPremium(true);
                    authedPlayer.setSalt(null);
                    authedPlayer.setPassword(null);
                    AuthedPlayerFactory.save(authedPlayer);

                    ProxiedPlayer player = plugin.getProxy().getPlayer(args[1]);
                    if (player != null) {
                        player.disconnect(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().premiumAdmin, sender.getName()));
                    }

                    sender.sendMessage(new ComponentBuilder("Der Account von ").color(GOLD)
                            .append(String.format("%s {UUID=%s}", args[1], authedPlayer.getUuid())).color(ChatColor.YELLOW)
                            .append(" wurde als Premium markiert.").color(GOLD).create());
                }
                return;
            case "free":
                if (args.length < 2) {
                    sendAll(sender, HELP_COMPONENTS);
                } else {
                    int amount = 4;

                    if (args.length > 2 && org.apache.commons.lang3.StringUtils.isNumeric(args[2])) {
                        amount = Integer.parseInt(args[2]);
                    }

                    AuthedPlayer[] matches = AuthedPlayerFactory.getByCriteria(args[1]);

                    boolean doMatchCheck = !args[1].startsWith("/");

                    if (matches.length == 0 && !doMatchCheck) {
                        sender.sendMessage(new ComponentBuilder("Für dein Kriterium wurde kein Benutzer gefunden.").color(RED).create());
                        return;
                    }

                    if (doMatchCheck) {
                        Set<String> freedIps = new HashSet<>();
                        for (AuthedPlayer authedPlayer : matches) {
                            if (!freedIps.contains(authedPlayer.getLastIp())) {
                                if (authedPlayer.getLastIp() == null) {
                                    sender.sendMessage(new ComponentBuilder("Für folgenden Spieler gibt es keine letzte IP: ").color(GOLD)
                                            .append(authedPlayer.toShortString()).color(YELLOW).create());
                                } else {
                                    IpAddressFactory.free(authedPlayer.getLastIp(), amount);
                                    freedIps.add(authedPlayer.getLastIp());
                                }
                            }
                        }

                        sender.sendMessage(new ComponentBuilder("Folgenden IPs wurden ").color(GOLD)
                                .append(String.valueOf(amount)).color(ChatColor.YELLOW)
                                .append(" Slots zugewiesen: ").color(GOLD)
                                .append(CommandHelper.CSCollection(freedIps)).color(ChatColor.YELLOW)
                                .append(". Zugehörige Benutzer: ").color(GOLD)
                                .append(CommandHelper.CSCollectionShort(Arrays.asList(matches)))
                                .create());
                    } else {
                        IpAddressFactory.free(args[1], amount);
                        sender.sendMessage(new ComponentBuilder("Der IP ").color(GOLD)
                                .append(args[1]).color(YELLOW)
                                .append(" wurden ").color(GOLD)
                                .append(String.valueOf(amount)).color(YELLOW)
                                .append(" Slots zugewiesen. Keine zugehörigen Benutzer gefunden.").color(GOLD).create());
                    }

                }
                return;
            case "user":
                if (args.length < 2) {
                    sendAll(sender, HELP_COMPONENTS);
                } else {
                    AuthedPlayer[] matches = AuthedPlayerFactory.getByCriteria(args[1]);

                    if (matches.length == 0) {
                        sender.sendMessage(new ComponentBuilder("Für dein Kriterium wurde kein Benutzer gefunden.").color(RED).create());
                        return;
                    }

                    for (AuthedPlayer match : matches) {
                        sender.sendMessage(new ComponentBuilder("Name: ").color(GOLD)
                                .append(match.getName()).color(ChatColor.YELLOW).create());
                        sender.sendMessage(new ComponentBuilder("UUID: ").color(GOLD)
                                .append(match.getUuid()).color(YELLOW).create());
                        sender.sendMessage(new ComponentBuilder("Premium? ").color(GOLD)
                                .append(match.isPremium() ? "ja" : "nein").color(match.isPremium() ? GREEN : RED).create());
                        sender.sendMessage(new ComponentBuilder("Authentifiziert: ").color(GOLD)
                                .append(String.valueOf(match.getAuthenticationProvider())).color(YELLOW).create());
                        sender.sendMessage(new ComponentBuilder("Letzte IP: ").color(GOLD)
                                .append(match.getLastIp()).color(YELLOW).create());
                        IpAddress ip = IpAddressFactory.get(match.getLastIp());
                        sender.sendMessage(new ComponentBuilder("IP-Slots: ").color(GOLD)
                                .append(ip == null ? null : String.valueOf(ip.getMaxUsers())).color(YELLOW).create());
                        sender.sendMessage(new ComponentBuilder("IP-Sessions: ").color(GOLD)
                                .append(ip == null ? null : String.valueOf(ip.isSessionsEnabled()))
                                .color(ip == null ? GRAY : ip.isSessionsEnabled() ? GREEN : RED).create()); //red/green if non-null, gray if null
                    }
                }
                return;
            case "unregister":
                if (args.length < 2) {
                    sendAll(sender, HELP_COMPONENTS);
                } else {
                    AuthedPlayer[] matches = AuthedPlayerFactory.getByCriteria(args[1]);

                    if (matches.length > 1 && !(args.length > 2 && args[2].equals("-R"))) {
                        sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium wurden mehr als ein Spieler gefunden. " +
                                "Bitte verwende -R am Ende, um mehrere User zu löschen. Gefundene User: ").color(GOLD)
                                .append(CommandHelper.CSCollectionShort(Arrays.asList(matches))).create());
                        return;
                    }

                    if (matches.length == 0) {
                        sender.sendMessage(new ComponentBuilder("Für dein Kriterium wurde kein Benutzer gefunden.").color(RED).create());
                        return;
                    }

                    for (AuthedPlayer match : matches) {
                        sender.sendMessage(new ComponentBuilder("Permanently deleted this player: ").color(RED)
                                .append(String.valueOf(match)).color(DARK_RED).create());
                        XLoginPlugin.AUTHED_PLAYER_REPOSITORY.deletePlayer(match);
                    }
                }
                return;
            case "forcecrack":

                return;
            default:
                sendAll(sender, HELP_COMPONENTS);
        }
    }

    private void sendAll(CommandSender sender, BaseComponent[][] allComponents) {
        for (BaseComponent[] components : allComponents) {
            sender.sendMessage(components);
        }
    }
}
