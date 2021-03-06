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

package li.l1t.xlogin.bungee.punishment.warn;

import com.google.common.collect.ImmutableSet;
import li.l1t.common.bungee.ChatHelper;
import li.l1t.xlogin.bungee.JSONChatHelper;
import li.l1t.xlogin.common.api.punishments.XLoginWarning;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A command which lists warnings per user
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29.8.14
 */
class CommandListWarns extends Command implements TabExecutor {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final WarnModule module;

    public CommandListWarns(WarnModule module) {
        super("warns", null, "lw");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            UUID senderId = ChatHelper.getSenderId(sender);
            listWarnings(sender, module.getPlugin().getRepository().getProfile(senderId), module.getWarningsByTarget(senderId));
        } else if (!args[0].equalsIgnoreCase("help")) {
            List<AuthedPlayer> matchedPlayers = module.getPlugin().getRepository().getProfiles(args[0]);
            if (matchedPlayers.isEmpty()) {
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
            } else if (matchedPlayers.size() > 1) {
                JSONChatHelper.listPossiblePlayers(sender, matchedPlayers, module.getPlugin(), "Hier klicken, um die Verwarnungen\n dieses Spielers zu zeigen",
                        "/warns %s");
                sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium sind zu viele Benutzer vorhanden: " + matchedPlayers.size())
                        .color(ChatColor.RED).create());
            } else {
                listWarnings(sender, matchedPlayers.get(0), module.getWarningsByTarget(matchedPlayers.get(0).getUniqueId()));
            }
        } else {
            sender.sendMessage(new ComponentBuilder("/warns [Spieler] - Zeigt Verwarnungen an.").color(ChatColor.YELLOW).create());
        }
    }

    private void listWarnings(CommandSender receiver, AuthedPlayer target, List<WarningInfo> warnings) {
        if (warnings.isEmpty()) {
            if (ChatHelper.getSenderId(receiver).equals(target.getUniqueId())) {
                receiver.sendMessage(new ComponentBuilder("Du hast keine Verwarnungen! :)").color(ChatColor.DARK_GREEN).create());
                return;
            }
            receiver.sendMessage(new ComponentBuilder(target.getName() + " hat keine Verwarnungen!").color(ChatColor.GREEN).create());
            return;
        }

        int validWarnings = 0;
        for (WarningInfo warningInfo : warnings) {
            if (warningInfo.isValid()) {
                validWarnings++;
            }
        }

        ComponentBuilder headerBuilder = new ComponentBuilder("==== Zeige ").color(ChatColor.GOLD)
                .append(validWarnings + " Verwarnungen von " + target.getName()).color(ChatColor.YELLOW);

        if (receiver.hasPermission(CommandWarn.PERMISSION)) { //Display a link to warn the user if permitted
            headerBuilder.append(" [+] ").color(ChatColor.DARK_GREEN).underlined(true)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warn " + target.getName() + " "))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hier klicken zum Verwarnen").create()));
        }

        receiver.sendMessage(headerBuilder.append(" ====").color(ChatColor.GOLD).underlined(false).create());

        receiver.sendMessage(new ComponentBuilder("UUID: ").color(ChatColor.GOLD)
                .append(target.getUuid()).color(ChatColor.YELLOW).create());

        if (module.getBanModule() != null) {
            ComponentBuilder banInfoBuilder = new ComponentBuilder("gebannt? ").color(ChatColor.GOLD);
            if (module.getBanModule().isBanned(target.getUniqueId())) {
                banInfoBuilder.append("ja ").color(ChatColor.DARK_RED)
                        .append("[Mehr Info]").color(ChatColor.YELLOW).underlined(true)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/baninfo " + target.getUniqueId()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/baninfo " + target.getUniqueId()).create()));
            } else {
                banInfoBuilder.append("nein").color(ChatColor.GRAY);
            }
            receiver.sendMessage(banInfoBuilder.create());
        }

        for (WarningInfo warn : warnings) {
            boolean unknownReason = warn.getState() == XLoginWarning.WarningState.UNKNOWN_REASON;
            boolean invalid = warn.getState() == XLoginWarning.WarningState.INVALID;

            ComponentBuilder warnBuilder = new ComponentBuilder((unknownReason ? "!" : "#") + warn.getId());
            if (unknownReason) {
                warnBuilder.color(ChatColor.RED);
            } else if (invalid) {
                warnBuilder.color(ChatColor.GRAY).strikethrough(true);
            } else {
                warnBuilder.color(ChatColor.GRAY);
            }

            warnBuilder.append(" von " + warn.getSourceName(module.getPlugin().getRepository())).color(ChatColor.GOLD)
                    .append(" am " + SIMPLE_DATE_FORMAT.format(warn.getDate())).color(ChatColor.YELLOW)
                    .append(" auf " + warn.getSourceServerName()).color(ChatColor.GOLD);

            if (receiver.hasPermission(CommandDeleteWarn.PERMISSION)) {
                warnBuilder.append(" [-] ").color(ChatColor.DARK_RED).underlined(true)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/delwarn -i " + warn.getId()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hier klicken, um diese Verwarnung zu löschen.").create()));
            }

            receiver.sendMessage(warnBuilder.create());
            receiver.sendMessage(TextComponent.fromLegacyText((invalid ? "§e§m  " : "  ") + warn.getReason()));
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
