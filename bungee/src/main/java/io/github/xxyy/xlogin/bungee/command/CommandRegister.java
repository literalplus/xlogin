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
import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.TimeUnit;

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
            plugin.getRateLimitManager().blockIpFor(plr.getAddress(), 1, TimeUnit.HOURS);
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

        if (plugin.getRateLimitManager().getRegisterLimit().incrementAndCheck()){
            plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().registerRateLimit));
            return;
        }

        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());
        assert authedPlayer != null;
        authedPlayer.registerPassword(args[0], plr.getAddress().getAddress().toString());
        plugin.getRegistry().registerAuthentication(authedPlayer);

        plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().successfullyAuthenticated));

        plugin.getRepository().updateProfile(authedPlayer);

        AuthedPlayerFactory.save(authedPlayer);
        plugin.notifyRegister(plr);
        plugin.notifyAuthentication(plr, authedPlayer);
    }
}
