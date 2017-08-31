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
import li.l1t.common.lib.com.mojang.api.profiles.HttpProfileRepository;
import li.l1t.common.lib.com.mojang.api.profiles.Profile;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Marks a user to have a Mojang premium account.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
public class CommandPremium extends Command {
    public static final HttpProfileRepository HTTP_PROFILE_REPOSITORY = new HttpProfileRepository("minecraft");
    private final XLoginPlugin plugin;

    public CommandPremium(XLoginPlugin plugin) {
        super("premium", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(ChatHelper.kickConsoleFromMethod(sender)){
            return;
        }

        ProxiedPlayer plr = (ProxiedPlayer) sender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandPremiumUsage));
                return;
            }
        }

        if(!plugin.getRegistry().isAuthenticated(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));
            return;
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());

        if(authedPlayer.isPremium()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().accountAlreadyPremium));
            return;
        }

        Profile[] profiles = HTTP_PROFILE_REPOSITORY.findProfilesByNames(plr.getName());

        if(profiles.length == 0) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().accountNotPremium));
            return;
        } else if(profiles.length > 1) {
            plr.sendMessage(new ComponentBuilder("Für deinen Namen gibt es mehrere Accounts. Das sollte nicht passieren. " +
                    "Bitte melde dies an das Team und kontaktiere den Mojang-Support! " +
                    "Wir entschuldigen uns für die von Mojang verursachten Unannehmlichkeiten.").color(ChatColor.RED).create());
            return;
        }

        if(args.length < 0 || !args[0].equalsIgnoreCase("sicher")) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().premiumWarning));
            return;
        }


        authedPlayer.setPremium(true);
        authedPlayer.setPassword(null);
        authedPlayer.setSalt(null);
        authedPlayer.setSessionsEnabled(false);

        plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().accountMarkedPremium));

        AuthedPlayerFactory.save(authedPlayer);

        plr.disconnect(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().accountMarkedPremium)); //Kick player to ensure that it works
    }
}
