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
