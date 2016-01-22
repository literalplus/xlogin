/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.punishment.ban;

import com.google.common.collect.ImmutableSet;
import io.github.xxyy.xlogin.bungee.JSONChatHelper;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Command which removes bans from players.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30.8.14
 */
class CommandUnBan extends Command implements TabExecutor {
    public static final String PERMISSION = "xlogin.unban";
    private final BanModule module;

    public CommandUnBan(BanModule module) {
        super("unban", PERMISSION, "ub");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("Verwendung: ").color(ChatColor.GOLD)
                    .append("/unban <Spieler>").color(ChatColor.YELLOW).create());
            return;
        }

        List<AuthedPlayer> matchedPlayers = module.getPlugin().getRepository().getProfiles(args[0]);
        if (matchedPlayers.isEmpty()) {
            sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
            return;
        } else if (matchedPlayers.size() > 1) {
            JSONChatHelper.listPossiblePlayers(sender, matchedPlayers, module.getPlugin(), "Hier klicken, um diesen Spiler\nzu entbannen!",
                    "/ub %s");
            return;
        }

        BanInfo banInfo = module.getBanInfo(matchedPlayers.get(0).getUniqueId());

        if (banInfo == null) {
            sender.sendMessage(new ComponentBuilder(matchedPlayers.get(0).getName()).color(ChatColor.DARK_GREEN)
                    .append(" ist nicht gebannt!").color(ChatColor.GREEN).create());
            return;
        }
        banInfo.delete();

        BaseComponent[] adminComponents = new ComponentBuilder(sender.getName()).color(ChatColor.DARK_GREEN)
                .append(" hat ").color(ChatColor.GREEN)
                .append(matchedPlayers.get(0).getName()).color(ChatColor.DARK_GREEN)
                .append(" entbannt!").color(ChatColor.GREEN).create();

        for (ProxiedPlayer plr : module.getPlugin().getProxy().getPlayers()) {
            if (plr.hasPermission("xlogin.adminmsg")) {
                plr.sendMessage(adminComponents);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return ImmutableSet.of();
        }

        Set<String> matches = new HashSet<>();

        if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(search)) {
                    matches.add(player.getName());
                }
            }
        } else {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                matches.add(player.getName());
            }
        }

        return matches;
    }
}
