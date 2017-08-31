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
import li.l1t.xlogin.bungee.JSONChatHelper;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.md_5.bungee.api.ChatColor.*;

/**
 * Command which displays information about bans.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30.8.14
 */
class CommandBanInfo extends Command implements TabExecutor {
    private final BanModule module;

    public CommandBanInfo(BanModule module) {
        super("baninfo", "xlogin.baninfo", "bi");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("/baninfo <Spieler>").color(GOLD).create());
            return;
        }

        List<AuthedPlayer> matchedPlayers = module.getPlugin().getRepository().getProfiles(args[0]);
        if (matchedPlayers.isEmpty()) {
            sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
            return;
        } else if (matchedPlayers.size() > 1) {
            JSONChatHelper.listPossiblePlayers(sender, matchedPlayers, module.getPlugin(), "Hier klicken für Informationen\nzu diesem Spieler!",
                    "/bi %s");
            return;
        }

        AuthedPlayer match = matchedPlayers.get(0);
        BanInfo banInfo = module.getBanInfo(match.getUniqueId());

        if (banInfo == null) {
            ComponentBuilder cb = new ComponentBuilder(match.getName()).color(ChatColor.DARK_GREEN)
                    .append(" ist nicht gebannt!").color(ChatColor.GREEN);

            if (sender.hasPermission(CommandBan.PERMISSION)) {
                cb.append(" [bannen]").color(RED).underlined(true)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ban " + match.getName() + " "))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hier klicken zum Bannen!").create()));
            }

            sender.sendMessage(cb.create());
            return;
        }

        sender.sendMessage(new ComponentBuilder("==== ").color(GOLD)
                .append("BanInfo: " + match.getName()).color(YELLOW)
                .append(" ====").color(GOLD).create());

        ComponentBuilder uuidBuilder = new ComponentBuilder("UUID: ").color(GOLD)
                .append(match.getUniqueId().toString()).color(YELLOW);

        if (sender.hasPermission("xlogin.admin")) {
            uuidBuilder.append(" [Info]").color(RED).underlined(true)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xlo user " + match.getUniqueId().toString()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Premium? ").color(GOLD)
                                    .append(match.isPremium() ? "ja\n" : "nein\n").color(match.isPremium() ? GREEN : RED)
                                    .append("Authentifiziert: ").color(GOLD)
                                    .append(String.valueOf(match.getAuthenticationProvider()) + "\n").color(YELLOW)
                                    .append("Letzte IP: ").color(GOLD)
                                    .append(String.valueOf(match.getLastIp())).color(YELLOW)
                                    .append("\n[Klick für mehr Info]")
                                    .create()
                    ));
        }
        sender.sendMessage(uuidBuilder.create());

        sender.sendMessage(new ComponentBuilder("gebannt von: ").color(GOLD)
                .append(String.valueOf(banInfo.getSourceName(module.getPlugin().getRepository()))).color(YELLOW).create());
        sender.sendMessage(new ComponentBuilder("gebannt um: ").color(GOLD)
                .append(banInfo.getTimestampString()).color(YELLOW).create());

        ComponentBuilder bannedUntilBuilder = new ComponentBuilder("gebannt bis: ").color(GOLD)
                .append(banInfo.getExpiryString()).color(YELLOW);

        if (sender.hasPermission(CommandUnBan.PERMISSION)) {
            bannedUntilBuilder.append(" [entbannen]").color(GREEN).underlined(true)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unban " + match.getName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/unban " + match.getName()).create()));
        }

        sender.sendMessage(bannedUntilBuilder.create());

        sender.sendMessage(new ComponentBuilder("Grund: ").color(GOLD)
                .append(banInfo.getReason()).color(YELLOW).create());
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
