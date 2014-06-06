package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
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
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getPlayer(plr.getUniqueId(), plr.getName());

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandSessionsUsage));
                return;
            } //Just show state

            boolean newState = false;

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

            authedPlayer.setSessionsEnabled(newState);

            AuthedPlayerFactory.save(authedPlayer);
        }

        if (authedPlayer.isSessionsEnabled()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsEnabled));
        } else {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsDisabled));
        }

    }
}
