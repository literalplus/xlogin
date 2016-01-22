/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.command;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.common.lib.com.mojang.api.profiles.HttpProfileRepository;
import io.github.xxyy.common.lib.com.mojang.api.profiles.Profile;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
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
