package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.ips.SessionHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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

        if(!XLoginPlugin.AUTHED_PLAYER_REPOSITORY.isPlayerKnown(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notRegistered));
            return;
        }

        if(XLoginPlugin.AUTHED_PLAYER_REGISTRY.isAuthenticated(plr.getUniqueId())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().alreadyLoggedIn));
            return;
        }

        if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().commandLoginUsage));
            return;
        }

        AuthedPlayer authedPlayer = XLoginPlugin.AUTHED_PLAYER_REPOSITORY.getPlayer(plr.getUniqueId(), plr.getName());

        authedPlayer.setValid(true);

        if(!authedPlayer
                .authenticatePassword(args[0], plr.getAddress().getAddress().toString())) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().wrongPassword));
            return; //TODO: Only allow 5 tries, then tempban IP for 5m
        }

        XLoginPlugin.AUTHED_PLAYER_REGISTRY.registerAuthentication(authedPlayer);
        plugin.sendAuthNotification(plr, authedPlayer);

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
