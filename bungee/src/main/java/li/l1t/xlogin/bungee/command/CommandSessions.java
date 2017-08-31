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

package li.l1t.xlogin.bungee.command;

import li.l1t.common.bungee.ChatHelper;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Handles enabling and disabling of sessions
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
public class CommandSessions extends Command {
    private final XLoginPlugin plugin;

    public CommandSessions(XLoginPlugin plugin) {
        super("sessions", null, "sitzungen");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (ChatHelper.kickConsoleFromMethod(sender)) {
            return;
        }

        ProxiedPlayer plr = (ProxiedPlayer) sender;

        if (!plugin.getRegistry().isAuthenticated(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));
            return;
        } else if(!plugin.getConfig().isEnableSessions()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsUnavailable));
            return;
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());

        if(authedPlayer.isPremium()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsPremium));
            return;
        }

        if (args.length >= 1) {
            boolean newState;

            switch (args[0]) {
                case "on":
                case "an":
                    newState = true;
                    break;
                case "off":
                case "aus":
                    newState = false;
                    break;
                case "toggle":
                    newState = !authedPlayer.isSessionsEnabled();
                    break;
                default:
                    plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandSessionsUsage));
                    return;
            }

            authedPlayer.setSessionsEnabled(newState); //FIXME probably handled by IP or so

            AuthedPlayerFactory.save(authedPlayer);
        }

        if (authedPlayer.isSessionsEnabled()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsEnabled));
        } else {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsDisabled));
        }

    }
}
