package io.github.xxyy.xlogin.bungee.punishment.ban;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;

/**
 * Command which removes bans from players.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30.8.14
 */
class CommandUnBan extends Command {
    public static final String PERMISSION = "xlogin.unban";
    private final BanModule module;

    public CommandUnBan(BanModule module) {
        super("unban", PERMISSION, "ub");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("Verwendung: ").color(ChatColor.GOLD)
                    .append("/unban <Spieler>").color(ChatColor.YELLOW).create());
            return;
        }

        AuthedPlayer[] matchedPlayers = AuthedPlayerFactory.getByCriteria(args[0], module.getPlugin().getRepository());
        if (matchedPlayers.length == 0) {
            sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
            return;
        } else if (matchedPlayers.length > 1) {
            sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium sind zu viele Benutzer vorhanden: " + matchedPlayers.length)
                    .color(ChatColor.RED).create());
            return;
        }

        BanInfo banInfo = module.getBanInfo(matchedPlayers[0].getUniqueId());

        if (banInfo == null) {
            sender.sendMessage(new ComponentBuilder(matchedPlayers[0].getName()).color(ChatColor.DARK_GREEN)
                    .append(" ist nicht gebannt!").color(ChatColor.GREEN).create());
            return;
        }
        banInfo.delete();
        module.setBanned(matchedPlayers[0].getUniqueId(), null);

        BaseComponent[] adminComponents = new ComponentBuilder(sender.getName()).color(ChatColor.DARK_GREEN)
                .append(" hat ").color(ChatColor.GREEN)
                .append(matchedPlayers[0].getName()).color(ChatColor.DARK_GREEN)
                .append(" entbannt!").color(ChatColor.GREEN).create();

        for (ProxiedPlayer plr : module.getPlugin().getProxy().getPlayers()) {
            if (plr.hasPermission("xlogin.adminmsg")) {
                plr.sendMessage(adminComponents);
            }
        }
    }
}
