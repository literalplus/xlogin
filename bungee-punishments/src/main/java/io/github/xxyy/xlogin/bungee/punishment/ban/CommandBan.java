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

/**
 * Bans a player.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29.8.14
 */
class CommandBan extends Command {
    public static final String PERMISSION = "xlogin.ban.permanent";
    private final BanModule module;

    public CommandBan(BanModule module) {
        super("ban", PERMISSION, "rmhax");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("/ban <Spieler> <Grund>").color(ChatColor.YELLOW).create());
        } else {
            AuthedPlayer[] matchedPlayers = AuthedPlayerFactory.getByCriteria(args[0], module.getPlugin().getRepository());
            if (matchedPlayers.length == 0) {
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
                return;
            } else if (matchedPlayers.length > 1) {
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium sind zu viele Benutzer vorhanden: " + matchedPlayers.length)
                        .color(ChatColor.RED).create());
                return;
            }

            String reason = StringHelper.varArgsString(args, 1, true);
            BanInfo banInfo = BanInfoFactory.create(matchedPlayers[0].getUniqueId(), ChatHelper.getSenderId(sender),
                    sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getServer() : null,
                    reason, null);
            module.setBanned(matchedPlayers[0].getUniqueId(), banInfo);
            banInfo.announce(module);

            ProxiedPlayer targetPlayer = module.getPlugin().getProxy().getPlayer(matchedPlayers[0].getUniqueId());
            if (targetPlayer != null) {
                targetPlayer.disconnect(banInfo.createKickMessage());
            }
        }
    }
}
