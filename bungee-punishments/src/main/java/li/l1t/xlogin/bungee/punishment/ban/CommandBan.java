/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bans a player.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29.8.14
 */
class CommandBan extends Command implements TabExecutor {
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
            String reason = StringHelper.varArgsString(args, 1, true);

            List<AuthedPlayer> matchedPlayers = module.getPlugin().getRepository().getProfiles(args[0]);
            if (matchedPlayers.isEmpty()) {
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
                return;
            } else if (matchedPlayers.size() > 1) {
                JSONChatHelper.listPossiblePlayers(sender, matchedPlayers, module.getPlugin(), "Hier klicken, um\ndiesen Spieler zu bannen!",
                        "/ban %s " + reason.replace('§', '&'));
                return;
            }

            BanInfo banInfo = module.setBanned(matchedPlayers.get(0).getUniqueId(), ChatHelper.getSenderId(sender),
                    sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getServer().getInfo().getName() : null,
                    reason, null);
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
