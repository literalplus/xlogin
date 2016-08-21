/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.spigot.commands;

import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
import li.l1t.xlogin.spigot.XLoginPlugin;
import li.l1t.xlogin.spigot.listener.GenericListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Teleports players to spawn. wow.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
public class CommandLocalXLo implements CommandExecutor {
    private final XLoginPlugin plugin;

    public CommandLocalXLo(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("xlogin.admin")) {
            sender.sendMessage("no");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("/lxlo [debugp] - debug tool");
            sender.sendMessage(XLoginPlugin.VERSION);
            sender.sendMessage("skip=" + GenericListener.skip);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "debugp":
                if (args.length < 2) {
                    sender.sendMessage("/lxlo debugp <criteria>");
                    return true;
                }
                AuthedPlayer[] authedPlayers = AuthedPlayerFactory.getByCriteria(args[1], plugin.getRepository());

                if (authedPlayers.length == 0) {
                    sender.sendMessage("Keine Spieler gefunden.");
                    return true;
                }

                for (AuthedPlayer authedPlayer : authedPlayers) {
                    sender.sendMessage("§6Player: §e" + authedPlayer + "§6 authed=" + plugin.getRegistry().isAuthenticated(authedPlayer.getUniqueId()));
                }
                break;
            default:
                sender.sendMessage("Unknown action!");
        }

        return true;
    }

}
