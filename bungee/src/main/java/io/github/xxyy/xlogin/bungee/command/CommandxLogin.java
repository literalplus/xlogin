package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.ips.IpAddressFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

/**
 * Handles administrative commands.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 25.5.14
 */
public class CommandxLogin extends Command {
    private final XLoginPlugin plugin;
    private static final BaseComponent[][] HELP_COMPONENTS = {
            new ComponentBuilder("xLogin BungeeCord - Xtreme BungeeCord authentication system.").color(ChatColor.GOLD).create(),
            new ComponentBuilder("Copyright (C) 2014 xxyy98 aka Literallie - http://xxyy.github.io/").color(ChatColor.DARK_GRAY).create(),
            new ComponentBuilder("Version "+XLoginPlugin.PLUGIN_VERSION).color(ChatColor.DARK_GRAY).create(),
            new ComponentBuilder("/xlogin [help|reload] ").color(ChatColor.GOLD).append("Administrationsbefehl von xLogin.").color(ChatColor.GRAY).create(),
            new ComponentBuilder("/xlogin reload ").color(ChatColor.GOLD).append("Reloads data/configs from database & disk.").color(ChatColor.GRAY).create()
    };

    public CommandxLogin(XLoginPlugin plugin) {
        super("xlogin", null, "xlo");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendAll(sender, HELP_COMPONENTS);
            return;
        }

        switch(args[0].toLowerCase()) {
            case "reload":
                XLoginPlugin.AUTHED_PLAYER_REPOSITORY.clear();
                IpAddressFactory.clear();
                plugin.resetIpOnlinePlayers();
            default:
                sendAll(sender, HELP_COMPONENTS);
        }
    }

    private void sendAll(CommandSender sender, BaseComponent[][] allComponents) {
        for(BaseComponent[] components : allComponents) {
            sender.sendMessage(components);
        }
    }
}
