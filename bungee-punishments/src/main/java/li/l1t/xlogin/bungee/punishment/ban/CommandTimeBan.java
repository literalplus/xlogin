/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee.punishment.ban;

import com.google.common.collect.ImmutableSet;
import li.l1t.common.bungee.ChatHelper;
import li.l1t.common.util.StringHelper;
import li.l1t.xlogin.bungee.JSONChatHelper;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Temporarily bans a player.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29.8.14
 */
class CommandTimeBan extends Command implements TabExecutor {
    public static final String PERMISSION = "xlogin.ban";
    private final BanModule module;

    public CommandTimeBan(BanModule module) {
        super("timeban", PERMISSION, "tempban", "tban", "tb", "tempban");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("/tempban <Spieler> <Zeitraum> <Grund>").color(ChatColor.YELLOW).create());
            sender.sendMessage(new ComponentBuilder("Zeitraum: y=Jahr, M=Monat, d=Tag, h=Stunde, m=Minute, s=Sekunde").color(ChatColor.YELLOW).create());
            sender.sendMessage(new ComponentBuilder("Bsp: /tempban urmom 5y3M42m <Grund> -> Bannt 'urmom' 5 Jahre, 3 Monate und 42 Minuten lang.").color(ChatColor.YELLOW).create());
        } else {
            String reason = StringHelper.varArgsString(args, 2, true);
            List<AuthedPlayer> matchedPlayers = module.getPlugin().getRepository().getProfiles(args[0]);
            if (matchedPlayers.isEmpty()) {
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
                return;
            } else if (matchedPlayers.size() > 1) {
                JSONChatHelper.listPossiblePlayers(sender, matchedPlayers, module.getPlugin(), "Hier klicken, um diesen\nSPieler temporär zu bannen!",
                        String.format("/tb %%s %s %s", args[1], reason.replace('§', '&'))); //TODO: This should actually just implode the args
                return;
            }

            long timePeriod;
            try {
                timePeriod = StringHelper.parseTimePeriod(args[1]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(new ComponentBuilder("Invalides Zeitformat: ").color(ChatColor.DARK_RED)
                        .append(e.getMessage()).color(ChatColor.RED).create());
                return;
            }

            if (!sender.hasPermission("xlogin.ban.temporary") && timePeriod > TimeUnit.MILLISECONDS.convert(60, TimeUnit.DAYS)) {
                sender.sendMessage(new ComponentBuilder("Sorry, du darfst nur bis zu 2 Monate lang bannen!").color(ChatColor.RED).create());
                return;
            }

            BanInfo banInfo = module.setBanned(matchedPlayers.get(0).getUniqueId(), ChatHelper.getSenderId(sender),
                    sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getServer().getInfo().getName() : null,
                    reason,
                    new Date(System.currentTimeMillis() + timePeriod));
            banInfo.announce(module);

            ProxiedPlayer targetPlayer = module.getPlugin().getProxy().getPlayer(matchedPlayers.get(0).getUniqueId());
            if (targetPlayer != null) {
                targetPlayer.disconnect(banInfo.createKickMessage());
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return ImmutableSet.of();
        }

        Set<String> matches = new HashSet<>();

        if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(search)) {
                    matches.add(player.getName());
                }
            }
        } else {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                matches.add(player.getName());
            }
        }

        return matches;
    }
}
