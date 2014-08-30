package io.github.xxyy.xlogin.bungee.punishment.ban;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.common.util.StringHelper;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Temporarily bans a player.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29.8.14
 */
class CommandTimeBan extends Command {
    public static final String PERMISSION = "xlogin.ban";
    private final BanModule module;

    public CommandTimeBan(BanModule module) {
        super("timeban", PERMISSION, "tempban", "tban", "tb");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("/tempban <Spieler> <Zeitraum> <Grund>").color(ChatColor.YELLOW).create());
            sender.sendMessage(new ComponentBuilder("Zeitraum: y=Jahr, M=Monat, d=Tag, h=Stunde, m=Minute, s=Sekunde").color(ChatColor.YELLOW).create());
            sender.sendMessage(new ComponentBuilder("Bsp: /tempban urmom 5y3M42m <Grund> -> Bannt 'urmom' 5 Jahre, 3 Monate und 42 Minuten lang.").color(ChatColor.YELLOW).create());
        } else {
            AuthedPlayer[] matchedPlayers = AuthedPlayerFactory.getByCriteria(args[1], module.getPlugin().getRepository());
            if (matchedPlayers.length == 0) {
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
                return;
            } else if (matchedPlayers.length > 1) {
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium sind zu viele Benutzer vorhanden: " + matchedPlayers.length)
                        .color(ChatColor.RED).create());
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

            if (!sender.hasPermission("xlogin.ban.temporary") && timePeriod > TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS)) {
                sender.sendMessage(new ComponentBuilder("Sorry, du darfst nur bis zu 3 Tage lang bannen!").color(ChatColor.RED).create());
                return;
            }

            String reason = StringHelper.varArgsString(args, 2, true);
            BanInfo banInfo = BanInfoFactory.create(matchedPlayers[0].getUniqueId(), ChatHelper.getSenderId(sender),
                    sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getServer() : null,
                    reason,
                    new Date(System.currentTimeMillis() + timePeriod));

            module.setBanned(matchedPlayers[0].getUniqueId(), banInfo);
            banInfo.announce(module);

            ProxiedPlayer targetPlayer = module.getPlugin().getProxy().getPlayer(matchedPlayers[0].getUniqueId());
            if (targetPlayer != null) {
                targetPlayer.disconnect(banInfo.createKickMessage());
            }
        }
    }
}
