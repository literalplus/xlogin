package io.github.xxyy.xlogin.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.bungee.limits.AdaptiveRateLimit;
import io.github.xxyy.xlogin.bungee.limits.RateLimitManager;
import io.github.xxyy.xlogin.bungee.limits.SimpleRateLimit;

import java.util.Map;

import static net.md_5.bungee.api.ChatColor.BLUE;
import static net.md_5.bungee.api.ChatColor.DARK_GREEN;
import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.ITALIC;
import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.ChatColor.YELLOW;

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
    };
    private final XLoginPlugin plugin;

    public CommandxLoginLimit(XLoginPlugin plugin) {
        super("xlolimit", PERMISSION, "xlol");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")){
            sendAll(sender, HELP_COMPONENTS);
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
                if (!sender.hasPermission("xlogin.admin")){
                    sender.sendMessage(new ComponentBuilder("Du hast auf diesen Befehl keinen Zugriff!").color(RED).create());
                    return;
                }

                sender.sendMessage(new XyComponentBuilder("Current IP rate limits:").color(GOLD).create());
                for (Map.Entry<String, Integer> entry : manager.getIpJoins().entrySet()) {
                    if (entry.getValue() > 1){
                        sender.sendMessage(new XyComponentBuilder(" " + entry.getKey()).color(YELLOW)
                                .append(" -> ")
                                .append(String.valueOf(entry.getValue()),
                                        entry.getValue() > RateLimitManager.IP_JOIN_THRESHOLD ? RED : GREEN)
                                .create());
                    }
                }
                return;
            case "reset":
                if (!sender.hasPermission("xlogin.admin")){
                    sender.sendMessage(new ComponentBuilder("Du hast auf diesen Befehl keinen Zugriff!").color(RED).create());
                    return;
                }

                for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                    if (player.hasPermission("xlogin.cmd")){
                        sender.sendMessage(new ComponentBuilder("[" + sender.getName() + ": Reset xLogin limits]")
                                .color(GRAY).italic(true).create());
                    }
                }
                manager.getJoinLimit().reset();
                if (manager.getRegisterLimit() instanceof AdaptiveRateLimit){
                    ((AdaptiveRateLimit) manager.getRegisterLimit()).resetThreshold();
                }
                manager.getRegisterLimit().reset();
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

        if (rateLimit instanceof AdaptiveRateLimit){
            builder.append(" (adaptive: base=", BLUE, ITALIC)
                    .append(String.valueOf(((AdaptiveRateLimit) rateLimit).getBaseThreshold()))
                    .append(")");
        }

        sender.sendMessage(builder.create());
    }

    private void sendAll(CommandSender sender, BaseComponent[][] allComponents) {
        for (BaseComponent[] components : allComponents) {
            sender.sendMessage(components);
        }
    }
}
