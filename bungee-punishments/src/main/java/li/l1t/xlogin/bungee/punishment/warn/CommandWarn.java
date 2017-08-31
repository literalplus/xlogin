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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import li.l1t.common.bungee.ChatHelper;
import li.l1t.common.util.StringHelper;
import li.l1t.xlogin.bungee.JSONChatHelper;
import li.l1t.xlogin.bungee.XLoginBungee;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.ChatColor.YELLOW;

/**
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
class CommandWarn extends Command implements TabExecutor {
    public static final String PERMISSION = "xlogin.warn";
    private final Set<UUID> rateLimitPlayers = Collections.synchronizedSet(new HashSet<UUID>());
    private final XLoginBungee plugin;
    private final WarnModule module;

    public CommandWarn(WarnModule module) {
        super("warn", PERMISSION, "verwarnen", "xwarn");
        this.module = module;
        this.plugin = module.getPlugin();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("§c/warn <Spieler> [Anzahl] <Grund>").color(RED).create());
            return;
        }

        int multiplier = 1; //How many fu..um..warns to give
        int reasonStartIndex = 1;
        if (StringUtils.isNumeric(args[1])) {
            multiplier = Integer.parseInt(args[1]);
            reasonStartIndex = 2;

            if (args.length < 3) {
                sender.sendMessage(new ComponentBuilder("§c/warn <Spieler> [Anzahl] <Grund>").color(RED).create());
                return;
            }
        }
        if (multiplier > 15) {
            sender.sendMessage(plugin.getMessages().parseMessageWithPrefix("§eDas sind aber viele Warns! Ich habe dir die mal auf 15 reduziert."));
            multiplier = 15;
        }

        String reason = StringHelper.varArgsString(args, reasonStartIndex, true);

        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        UUID uuid;
        String name;
        if (target == null) {
            List<AuthedPlayer> players = plugin.getRepository().getProfiles(args[0]);
            if (players.isEmpty()) {
                plugin.getMessages().sendMessageWithPrefix("§eSorry, so einen Spieler kennen wir nicht.", sender);
                return;
            } else if (players.size() > 1) {
                JSONChatHelper.listPossiblePlayers(sender, players, plugin, "Hier klicken, um diesen Spieler zu verwarnen!",
                        String.format("/warn %%s %d %s", multiplier, reason.replace('§', '&')));
                return;
            } else {
                uuid = players.get(0).getUniqueId();
                name = players.get(0).getName();
            }
        } else {
            uuid = target.getUniqueId();
            name = target.getName();
        }

        if (rateLimitPlayers.contains(uuid)) {
            plugin.getMessages().sendMessageWithPrefix("Bitte warte 20 Sekunden, bevor du diesen Spieler erneut warnst!", sender);
            return;
        }

        for (int i = 0; i < multiplier; i++) {
            module.createWarning(uuid,
                    ChatHelper.getSenderId(sender),
                    sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getServer() : null,
                    ChatColor.translateAlternateColorCodes('&', reason));
        }

        for (ProxiedPlayer plr : plugin.getProxy().getPlayers()) {
            if (plr.hasPermission("xlogin.adminmsg")) {
                plugin.getMessages().sendMessage("§6" + name + "§c wurde von §6" + sender.getName() + "§c gewarnt:", plr);
            }

            ImmutableList.Builder<BaseComponent> headerComponents = ImmutableList.builder();
            headerComponents.addAll(Arrays.asList(plugin.getMessages().jsonPrefix));
            headerComponents.add(new ComponentBuilder(name).color(YELLOW)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warns " + name))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hier klicken, um\n").append("alle Warnungen anzuzeigen.").create()))
                    .create());
            headerComponents.add(TextComponent.fromLegacyText(MessageFormat.format(plugin.getMessages().warnBroadcastHeader, multiplier)));
            plr.sendMessage(headerComponents.build().toArray(new BaseComponent[3]));
            plugin.getMessages().sendMessage(plugin.getMessages().warnBroadcastBody, plr, reason);
        }


        rateLimitPlayers.add(uuid);
        final UUID finalUUID = uuid; //workaround for final thing
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                rateLimitPlayers.remove(finalUUID);
            }
        }, 20, TimeUnit.SECONDS);

        WarnPunishmentBuilder.compute(module, target, uuid, name);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length > 3) {
            return ImmutableSet.of();
        }

        Set<String> matches = new HashSet<>();

        if (args.length == 0) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                matches.add(player.getName());
            }
        } else if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(search)) {
                    matches.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            return ImmutableList.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10");
        } else {
            return ImmutableList.of("Werbung", "Beleidigung", "Spam", "Zeichenspam", "Bugusing", "Commandspam");
        }

        return matches;
    }
}
