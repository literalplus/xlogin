/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
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
