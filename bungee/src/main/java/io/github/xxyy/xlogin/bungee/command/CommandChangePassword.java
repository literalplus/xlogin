package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.common.util.encryption.PasswordHelper;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.CommandSender;
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

        if (!XLoginPlugin.AUTHED_PLAYER_REGISTRY.isAuthenticated(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));
            return;
        }

        AuthedPlayer authedPlayer = XLoginPlugin.AUTHED_PLAYER_REPOSITORY.getPlayer(plr.getUniqueId(), plr.getName());

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

        plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordChanged));
    }
}
