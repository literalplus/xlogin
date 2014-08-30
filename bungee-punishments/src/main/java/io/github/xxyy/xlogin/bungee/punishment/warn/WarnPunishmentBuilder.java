package io.github.xxyy.xlogin.bungee.punishment.warn;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
class WarnPunishmentBuilder {
    private final WarnModule module;
    private final ProxiedPlayer target;
    private final UUID targetId;
    private final String targetName;

    public WarnPunishmentBuilder(WarnModule module, ProxiedPlayer target, UUID targetId, String targetName) {
        this.module = module;
        this.target = target;
        this.targetId = targetId;
        this.targetName = targetName;
    }

    public WarnPunishmentBuilder compute() { //#spigot [0509] <Akkarin> there is no real limit afaik (apart from the packet limits)
        List<WarningInfo> warnings = WarningInfoFactory.fetchByTarget(targetId);
        int warningsTotal = warnings.size();

        if (warningsTotal == 0) {
            return this;
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
                    mostRecentWarning.getSourceServerName(), "10 Warnungen erreicht!", null);
        } else {
            int banHours = (int) Math.ceil((warningsTotal / 3) * 2);
            cb.append("für " + banHours + " Stunden gebannt!\n").color(ChatColor.RED);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, banHours);

            module.getBanModule().setBanned(targetId, mostRecentWarning.getSourceId(), mostRecentWarning.getSourceServerName(),
                    warningsTotal + " Warnungen erreicht!", cal.getTime());
        }

        cb.append("Letzter Warngrund:\n" + ChatColor.stripColor(mostRecentWarning.getReason())).color(ChatColor.YELLOW)
                .append("[Hier klicken für Liste deiner Verwarnungen]\n").color(ChatColor.GOLD).underlined(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.minotopia.me/?p=5&n=" + targetName))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("http://minotopia.me/?p=5&n=" + targetName).create()));

        if (warningsTotal < 10) {
            cb.append("Bei zehn Warnungen wirst du permanent gebannt.").color(ChatColor.RED).underlined(false);
        }

        target.disconnect(cb.create()); //TODO: The source needs to be informed of the punishment

        return this;
    }
}
