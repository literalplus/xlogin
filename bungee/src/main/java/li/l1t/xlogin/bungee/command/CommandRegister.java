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
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.TimeUnit;

/**
 * Handles registering users.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 21.5.14
 */
public class CommandRegister extends Command {
    private final XLoginPlugin plugin;

    public CommandRegister(XLoginPlugin plugin) {
        super("register", null, "registrieren");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (ChatHelper.kickConsoleFromMethod(sender)){
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


        if (args.length < 2 || args[0].equalsIgnoreCase("help")){
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandRegisterUsage));
            return;
        }

        if (!args[0].equals(args[1])){
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordsDontMatch));
            return;
        }

        if (args[0].length() < 5){
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordTooShort));
            return;
        }

        if (plr.getName().equalsIgnoreCase(args[0]) || plugin.getConfig().getUnsafePasswords().contains(args[0])){
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordInsecure));
            return;
        }

        if (plugin.getRateLimitManager().getRegisterLimit().incrementAndCheck()){
            plr.disconnect(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().registerRateLimit));
            return;
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());
        assert authedPlayer != null;

        if (alreadyRegisteredAndHasPassword(authedPlayer)){
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().alreadyRegistered));
            return;
        }

        authedPlayer.registerPassword(args[0], plr.getAddress().getAddress().toString());
        plugin.getRegistry().registerAuthentication(authedPlayer);

        plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().successfullyAuthenticated));

        plugin.getRepository().updateProfile(authedPlayer);

        AuthedPlayerFactory.save(authedPlayer);
        plugin.notifyRegister(plr);
        plugin.notifyAuthentication(plr, authedPlayer);
    }

    private boolean alreadyRegisteredAndHasPassword(AuthedPlayer authedPlayer) {
        return plugin.getRepository().isPlayerKnown(authedPlayer.getUniqueId()) &&
                authedPlayer.getPassword() != null;
    }
}
