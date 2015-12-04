package io.github.xxyy.xlogin.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;

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
            return;
        }

        if (plugin.getRepository().isPlayerKnown(plr.getUniqueId())){
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().alreadyRegistered));
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

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());
        authedPlayer.registerPassword(args[0], plr.getAddress().getAddress().toString());
        plugin.getRegistry().registerAuthentication(authedPlayer);

        plugin.getProxy().broadcast(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().welcome, plr.getName()));
        plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().successfullyAuthenticated));

        plugin.getRepository().updateProfile(authedPlayer);

        AuthedPlayerFactory.save(authedPlayer);
        plugin.notifyRegister(plr);
        plugin.sendAuthNotification(plr, authedPlayer);
    }
}
