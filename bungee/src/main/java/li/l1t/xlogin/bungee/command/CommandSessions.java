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
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
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
        } else if(!plugin.getConfig().isEnableSessions()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsUnavailable));
            return;
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());

        if(authedPlayer.isPremium()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsPremium));
            return;
        }

        if (args.length >= 1) {
            boolean newState;

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

            authedPlayer.setSessionsEnabled(newState); //FIXME probably handled by IP or so

            AuthedPlayerFactory.save(authedPlayer);
        }

        if (authedPlayer.isSessionsEnabled()) {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsEnabled));
        } else {
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsDisabled));
        }

    }
}
