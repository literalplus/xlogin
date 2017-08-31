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
import li.l1t.common.util.encryption.PasswordHelper;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command that allows to change your own password
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
public class CommandChangePassword extends Command {
    private final XLoginPlugin plugin;

    public CommandChangePassword(XLoginPlugin plugin) {
        super("cpw", null, "changepassword", "passwortändern");
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
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());

        if (args.length < 3 || args[0].equalsIgnoreCase("help")) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandCpwUsage));
            return;
        }

        if (!PasswordHelper.passwordsEqual(args[0], authedPlayer.getSalt(), authedPlayer.getPassword())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().oldPasswordIncorrect));
            return;
        }

        if (!args[1].equals(args[2])) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordsDontMatch));
            return;
        }

        if (args[1].length() < 5) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordTooShort));
            return;
        }

        if (plr.getName().equalsIgnoreCase(args[1]) || plugin.getConfig().getUnsafePasswords().contains(args[1])) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordInsecure));
            return;
        }

        String salt = PasswordHelper.generateSalt();

        authedPlayer.setSalt(salt);
        authedPlayer.setPassword(PasswordHelper.encrypt(args[1], salt));

        BaseComponent[] successBaseComponents = plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordChanged);
        plr.sendMessage(successBaseComponents);

        AuthedPlayerFactory.save(authedPlayer);

        plr.disconnect(successBaseComponents);
    }
}
