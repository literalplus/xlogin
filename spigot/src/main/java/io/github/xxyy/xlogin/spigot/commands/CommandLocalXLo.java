package io.github.xxyy.xlogin.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import io.github.xxyy.xlogin.spigot.XLoginPlugin;
import io.github.xxyy.xlogin.spigot.listener.GenericListener;

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
