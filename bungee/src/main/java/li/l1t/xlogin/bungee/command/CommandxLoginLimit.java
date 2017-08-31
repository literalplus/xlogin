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

package li.l1t.xlogin.bungee.command;

import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.bungee.limits.AdaptiveRateLimit;
import li.l1t.xlogin.bungee.limits.IpRateLimit;
import li.l1t.xlogin.bungee.limits.RateLimitManager;
import li.l1t.xlogin.bungee.limits.SimpleRateLimit;
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
