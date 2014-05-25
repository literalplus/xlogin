package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.common.lib.com.mojang.api.profiles.HttpProfileRepository;
import io.github.xxyy.common.lib.com.mojang.api.profiles.Profile;
import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.util.encryption.PasswordHelper;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import io.github.xxyy.xlogin.common.ips.IpAddressFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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
            new ComponentBuilder("xLogin BungeeCord - Xtreme BungeeCord authentication system.").color(ChatColor.GOLD).create(),
            new ComponentBuilder("Copyright (C) 2014 xxyy98 aka Literallie - http://xxyy.github.io/").color(ChatColor.DARK_GRAY).create(),
            new ComponentBuilder("Version " + XLoginPlugin.PLUGIN_VERSION).color(ChatColor.DARK_GRAY).create(),
            new ComponentBuilder("/xlogin [help|reload|cpw|premium] ").color(ChatColor.GOLD).append("Administrationsbefehl von xLogin.").color(ChatColor.GRAY).create(),
            new ComponentBuilder("/xlogin reload ").color(ChatColor.GOLD).append("Reloads data/configs from database & disk.").color(ChatColor.GRAY).create()
            new ComponentBuilder("/xlogin cpw [Name] [New password] ").color(ChatColor.GOLD).append("Changes password of a cracked account.").color(ChatColor.GRAY).create()
            new ComponentBuilder("/xlogin premium [Name] ").color(ChatColor.GOLD).append("Marks an account as premium. (Also changes UUID!!)").color(ChatColor.GRAY).create()
    };

    public CommandxLogin(XLoginPlugin plugin) {
        super("xlogin", null, "xlo");
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

                    sender.sendMessage(new ComponentBuilder("Das Passwort von ").color(ChatColor.GOLD)
                            .append(args[1]).color(ChatColor.YELLOW)
                            .append(" wurde geändert.").color(ChatColor.GOLD).create());
                }
                return;
            case "premium":
                if (args.length < 2) {
                    sendAll(sender, HELP_COMPONENTS);
                } else {
                    Profile[] profiles = HTTP_PROFILE_REPOSITORY.findProfilesByNames(args[1]);
                    if (profiles.length == 0 || profiles[0].getDemo()) {
                        sender.sendMessage(new ComponentBuilder("Für diesen Spieler wurde kein Premium-Account gefunden.").color(ChatColor.GOLD).create());
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

                    sender.sendMessage(new ComponentBuilder("Der Account von ").color(ChatColor.GOLD)
                            .append(String.format("%s {UUID=%s}", args[1], authedPlayer.getUuid())).color(ChatColor.YELLOW)
                            .append(" wurde als Premium markiert.").color(ChatColor.GOLD).create());
                    return;
                }
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
