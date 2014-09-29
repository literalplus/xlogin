package io.github.xxyy.xlogin.bungee.punishment.warn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.apache.commons.lang.StringUtils;

import io.github.xxyy.common.bungee.ChatHelper;
import io.github.xxyy.common.util.StringHelper;
import io.github.xxyy.xlogin.bungee.XLoginBungee;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.md_5.bungee.api.ChatColor.GOLD;
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

        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        UUID uuid;
        String name;
        if (target == null) {
            List<AuthedPlayer> players = plugin.getRepository().getProfiles(args[0]);
            if (players.isEmpty()) {
                plugin.getMessages().sendMessageWithPrefix("§eSorry, so einen Spieler kennen wir nicht.", sender);
                return;
            } else if (players.size() > 1) {
                plugin.getMessages().sendMessageWithPrefix("§cIch habe mehrere Spieler gefunden. Meintest du:", sender);
                for (AuthedPlayer authedPlayer : players) {
                    sender.sendMessage(new ComponentBuilder(authedPlayer.getName()).color(GOLD).create());
                }
                return;
            } else {
                uuid = players.get(0).getUniqueId();
                name = players.get(0).getName();
            }
        } else {
            uuid = target.getUniqueId();
            name = target.getName();
        }

        String reason = StringHelper.varArgsString(args, reasonStartIndex, true);

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
