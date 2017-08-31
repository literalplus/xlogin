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

import li.l1t.xlogin.bungee.punishment.ban.BanInfo;
import li.l1t.xlogin.common.api.punishments.XLoginWarning;
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

    public static void compute(WarnModule module, ProxiedPlayer target, UUID targetId, String targetName) { //#spigot [0509] <Akkarin> there is no real limit afaik (apart from the packet limits) [for quit message length]
        List<WarningInfo> dbWarnings = module.getWarningsByTarget(targetId);
        List<WarningInfo> warnings = new ArrayList<>();
        for (WarningInfo warn : dbWarnings) {
            if (warn.getState() == XLoginWarning.WarningState.VALID) {
                warnings.add(warn);
            }
        }
        BanInfo banInfo = module.getBanModule().getBanInfo(targetId);
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
            if (banInfo != null && banInfo.getExpiryTime() == null){
                return;
            }

            cb.append("permanent").color(ChatColor.DARK_RED).bold(true)
                    .append(" gebannt!\n").color(ChatColor.RED).bold(false);
            module.getBanModule().setBanned(targetId, mostRecentWarning.getSourceId(),
                    mostRecentWarning.getSourceServerName(), "10 Warnungen erreicht (" + mostRecentWarning.getReason() + ")!", null);
            ProxyServer.getInstance().broadcast(module.getPlugin().getMessages().parseMessageWithPrefix("§c" + targetName + "§6 wurde daher §4§lpermanent§6 gebannt."));
        } else {
            int banHours = (int) Math.ceil((warningsTotal / 3) * 2);
            cb.append("für " + banHours + " Stunden gebannt!\n").color(ChatColor.RED);

            Calendar cal = Calendar.getInstance();
            if (banInfo != null){
                if (banInfo.getExpiryTime() == null){
                    return;
                } else {
                    cal.setTime(banInfo.getExpiryTime()); //just add to existing temporary ban
                }
            }
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
