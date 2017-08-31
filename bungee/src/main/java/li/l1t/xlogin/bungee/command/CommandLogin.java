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
import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.ips.SessionHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.TimeUnit;

/**
 * Handles the /login command.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 16.5.14
 */
public class CommandLogin extends Command {
    private final XLoginPlugin plugin;

    public CommandLogin(XLoginPlugin plugin) {
        super("login");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(ChatHelper.kickConsoleFromMethod(sender)){
            return;
        }

        ProxiedPlayer plr = (ProxiedPlayer) sender;

        if (plr.getServer() == null){
            /*
            Legitimate players can't chat before they're connected to a server.
            Some joinbots do, however, not wait until the connection has been
            established before sending /register. Since there is no way to do
            this with a legit Vanilla client, we can kick them.
             */
            plr.disconnect(new XyComponentBuilder("Bist du ein Joinbot?").create());
            plugin.getRateLimitManager().blockIpFor(plr.getAddress(), 1, TimeUnit.HOURS);
            return;
        }

        if(!plugin.getRepository().isPlayerKnown(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notRegistered));
            return;
        }

        if(plugin.getRegistry().isAuthenticated(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().alreadyLoggedIn));
            return;
        }

        if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandLoginUsage));
            return;
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());

        authedPlayer.setValid(true, false);

        if(authedPlayer.getPassword() == null) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notRegistered));
            return;
        }

        if(!authedPlayer
                .authenticatePassword(args[0], plr.getAddress().getAddress().toString())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().wrongPassword));
            return; //TODO: Only allow 5 attempts, then tempban IP for 5m
        }

        plugin.getRegistry().registerAuthentication(authedPlayer);
        plugin.notifyAuthentication(plr, authedPlayer);

        plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().successfullyAuthenticated));


        //Last location
        plugin.teleportToLastLocation(plr);

        if(authedPlayer.isSessionsEnabled()) {
            SessionHelper.start(authedPlayer);
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsEngaged));
        }


//        //Failed login attempts
//        List<FailedLoginAttempt> attempts = EbeanManager.getEbean().find(FailedLoginAttempt.class)
//                .where().eq("user", authedPlayer)
//                .findList();
//
//        if(attempts.size() > 0) {
//            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().failedLoginAttemptsFound, attempts.size()));
//
//            for(FailedLoginAttempt attempt : attempts) {
//                plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().failedLoginAttemptItem,
//                        new Date(attempt.getTimestamp().getTime()), attempt.getIp()
//                        ));
//
//                EbeanManager.getEbean().delete(attempt);
//            }
//        }
    }
}
