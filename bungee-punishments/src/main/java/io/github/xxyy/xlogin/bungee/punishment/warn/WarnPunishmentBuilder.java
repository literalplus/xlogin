package io.github.xxyy.xlogin.bungee.punishment.warn;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Figures out how a player needs to be punished for the amount of warns they have accumulated.
 * It even executes the computed punishments!
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
final class WarnPunishmentBuilder {
    private WarnPunishmentBuilder() {

    }

    public static void compute(WarnModule module, ProxiedPlayer target, UUID targetId, String targetName) { //#spigot [0509] <Akkarin> there is no real limit afaik (apart from the packet limits)
        List<WarningInfo> dbWarnings = module.getWarningsByTarget(targetId);
        List<WarningInfo> warnings = new ArrayList<>();
        for (WarningInfo warn : dbWarnings) {
            if (warn.isValid()) {
                warnings.add(warn);
            }
        }
        int warningsTotal = warnings.size();

        if (warningsTotal == 0) {
            return;
        }

        WarningInfo mostRecentWarning = warnings.get(warningsTotal - 1);

        ComponentBuilder cb = new ComponentBuilder("[WARNUNG]\n").color(ChatColor.DARK_RED).bold(true)
                .append("Du hast ").color(ChatColor.RED).bold(false)
                .append(String.valueOf(warningsTotal)).color(ChatColor.RED).bold(true)
                .append(" Warns erreicht und wurdest daher ").color(ChatColor.RED).bold(false);

        if (warningsTotal <= 3 || module.getBanModule() == null) {
            cb.append("vom Server geworfen!\n").color(ChatColor.RED);
        } else if (warningsTotal >= 10) {
            cb.append("permanent").color(ChatColor.DARK_RED).bold(true)
                    .append(" gebannt!\n").color(ChatColor.RED).bold(false);
            module.getBanModule().setBanned(targetId, mostRecentWarning.getSourceId(),
                    mostRecentWarning.getSourceServerName(), "10 Warnungen erreicht (" + mostRecentWarning.getReason() + ")!", null);
            ProxyServer.getInstance().broadcast(module.getPlugin().getMessages().parseMessageWithPrefix("§c" + targetName + "§6 wurde daher §4§lpermanent§6 gebannt."));
        } else {
            int banHours = (int) Math.ceil((warningsTotal / 3) * 2);
            cb.append("für " + banHours + " Stunden gebannt!\n").color(ChatColor.RED);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, banHours);

            module.getBanModule().setBanned(targetId, mostRecentWarning.getSourceId(), mostRecentWarning.getSourceServerName(),
                    warningsTotal + " Warnungen erreicht! (" + mostRecentWarning.getReason() + ")", cal.getTime());
            ProxyServer.getInstance().broadcast(module.getPlugin().getMessages().parseMessageWithPrefix("§c" + targetName + "§6 wurde daher für " + banHours + " Stunden gebannt."));
        }

        cb.append("Letzter Warngrund:\n").color(ChatColor.GOLD)
                .append(ChatColor.stripColor(mostRecentWarning.getReason())).color(ChatColor.YELLOW)
                .append("\nListe deiner Verwarnungen:\n").color(ChatColor.GOLD)
                .append("http://www.minotopia.me/?p=5&n=" + targetName).color(ChatColor.YELLOW).underlined(true);

        if (warningsTotal < 10) {
            cb.append("\nBei zehn Warnungen wirst du permanent gebannt.").color(ChatColor.RED).underlined(false);
        }

        if (target != null) {
            target.disconnect(cb.create());
        }
    }
}
