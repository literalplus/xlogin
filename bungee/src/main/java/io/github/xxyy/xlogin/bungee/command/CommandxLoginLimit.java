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

import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.bungee.limits.AdaptiveRateLimit;
import io.github.xxyy.xlogin.bungee.limits.IpRateLimit;
import io.github.xxyy.xlogin.bungee.limits.RateLimitManager;
import io.github.xxyy.xlogin.bungee.limits.SimpleRateLimit;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import static net.md_5.bungee.api.ChatColor.*;

/**
 * A management command for rate limits and the alike enforced by xLogin.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10/10/15
 */
public class CommandxLoginLimit extends Command {
    public static final String PERMISSION = "xlogin.cmd";
    private static final BaseComponent[][] HELP_COMPONENTS = {
            new XyComponentBuilder("/xlol -  ").color(GOLD).append("Managementbefehl für xLogin-Limits").create(),
            new XyComponentBuilder("/xlol show ").color(GOLD).append("Zeigt aktuelle Rate-Limits.", GRAY).create(),
            new XyComponentBuilder("/xlol ips ").color(GOLD).append("Zeigt aktuelle IP-Limits.", GRAY).create(),
            new XyComponentBuilder("/xlol reset ").color(GOLD).append("Setzt die Limits zurück.", GRAY).create(),
            new XyComponentBuilder("/xlol stfu ").color(GOLD).append("Blendet Angriffswarnungen aus (oder wieder ein).", GRAY).create(),
    };
    private final XLoginPlugin plugin;

    public CommandxLoginLimit(XLoginPlugin plugin) {
        super("xlolimit", PERMISSION, "xlol");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendAll(sender, HELP_COMPONENTS);
            return;
        }

        if (!args[0].equalsIgnoreCase("show") && !sender.hasPermission("xlogin.admin")) {
            sender.sendMessage(new ComponentBuilder("Du hast auf diesen Befehl keinen Zugriff!").color(RED).create());
            return;
        }

        RateLimitManager manager = plugin.getRateLimitManager();

        switch (args[0].toLowerCase()) {
            case "show":
                sender.sendMessage(new XyComponentBuilder("Current rate limits:").color(GOLD).create());
                showRateLimit(sender, manager.getRegisterLimit(), "Register: ");
                showRateLimit(sender, manager.getJoinLimit(), "Join: ");
                return;
            case "ips":
                sender.sendMessage(new XyComponentBuilder("Current IP rate limits: (red = limited)").color(GOLD).create());
                int i = 0;
                for (IpRateLimit limit : manager.getIpRateLimits()) {
                    if (limit.getCurrentValue() > 1 || limit.getTimeLimit() > 1) {
                        showRateLimit(sender, limit, " ▶ ");
                        i++;
                    }
                }
                if (i == 0) {
                    sender.sendMessage(new XyComponentBuilder("(none)").color(DARK_GREEN).create());
                }
                return;
            case "reset":
                for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                    if (player.hasPermission("xlogin.cmd")) {
                        player.sendMessage(new ComponentBuilder("[" + sender.getName() + ": Reset xLogin limits]")
                                .color(GRAY).italic(true).create());
                    }
                }
                plugin.getLogger().info(sender.getName() + " reset xLogin limits");
                manager.resetAllLimits();
                return;
            case "stfu":
                if (!(sender instanceof ProxiedPlayer)) {
                    sender.sendMessage(new XyComponentBuilder("Nur Spieler können Angriffsnachrichten abschalten! :P").create());
                    return;
                }
                boolean ignoresNotices = manager.toggleIgnoresNotices(((ProxiedPlayer) sender).getUniqueId());
                sender.sendMessage(
                        new XyComponentBuilder("Benachrichtigungen über mögliche Angriffe sind für dich jetzt ").color(GOLD)
                                .append(ignoresNotices ? "ausgeschaltet" : "eingeschaltet").color(ignoresNotices ? RED : GREEN)
                                .append("!", GOLD).create()
                );
                return;
            default:
                sender.sendMessage(new XyComponentBuilder("Unbekannte Aktion. Hilfe:").color(RED).create());
                sendAll(sender, HELP_COMPONENTS);
        }
    }

    public void showRateLimit(CommandSender sender, SimpleRateLimit rateLimit, String limitName) {
        XyComponentBuilder builder = new XyComponentBuilder(limitName).color(GOLD)
                .append(rateLimit.getCurrentValue() +
                                " / " + rateLimit.getThreshold(),
                        rateLimit.isLimited() ? RED : DARK_GREEN);

        if (rateLimit instanceof AdaptiveRateLimit) {
            builder.append(" (adaptive: base=", BLUE, ITALIC)
                    .append(String.valueOf(((AdaptiveRateLimit) rateLimit).getBaseThreshold()))
                    .append(")");
        } else if (rateLimit instanceof IpRateLimit) {
            IpRateLimit ipRateLimit = (IpRateLimit) rateLimit;
            builder.append(" (ip: ", DARK_AQUA, ITALIC)
                    .append(ipRateLimit.getIpString())
                    .append(", time=")
                    .append(String.valueOf(ipRateLimit.getTimeLimit() * RateLimitManager.LIMIT_RESET_INTERVAL))
                    .append("s)");
        }

        sender.sendMessage(builder.create());
    }

    private void sendAll(CommandSender sender, BaseComponent[][] allComponents) {
        for (BaseComponent[] components : allComponents) {
            sender.sendMessage(components);
        }
    }
}
