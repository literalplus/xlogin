package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.common.util.encryption.PasswordHelper;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.Validate;

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
        if(ChatHelper.kickConsoleFromMethod(sender)){
            return;
        }

        ProxiedPlayer plr = (ProxiedPlayer) sender;

        if(XLoginPlugin.AUTHED_PLAYER_REPOSITORY.isPlayerKnown(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().alreadyRegistered));
            return;
        }

        if(args.length < 2 || args[0].equalsIgnoreCase("help")) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandRegisterUsage));
            return;
        }

        if(!args[0].equals(args[1])) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordsDontMatch));
            return;
        }

        if(args[0].length() < 5) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordTooShort));
            return;
        }

        if(plr.getName().equalsIgnoreCase(args[0]) || plugin.getConfig().getUnsafePasswords().contains(args[0])) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().passwordInsecure));
            return;
        }

        AuthedPlayer authedPlayer = XLoginPlugin.AUTHED_PLAYER_REPOSITORY.getPlayer(plr.getUniqueId(), plr.getName());

        String salt = PasswordHelper.generateSalt();

        authedPlayer.setSalt(salt);
        authedPlayer.setPassword(PasswordHelper.encrypt(args[0], salt));

        Validate.isTrue(authedPlayer.authenticatePassword(args[0], plr.getAddress().getAddress().toString()), "Setting password failed for registration!");
        XLoginPlugin.AUTHED_PLAYER_REGISTRY.registerAuthentication(authedPlayer);

        plugin.getProxy().broadcast(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().welcome));
        plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().successfullyAuthenticated));

        XLoginPlugin.AUTHED_PLAYER_REPOSITORY.updateKnown(plr.getUniqueId(), true);

        plugin.notifyRegister(plr);
    }
}
