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

package li.l1t.xlogin.bungee.notifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Notifies administrators if a player is using multiple accounts on a single IP address.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-22
 */
public class AltAccountNotifer {
    public static final long IGNORE_ALTS_OLDER_THAN_DAYS = 14;
    private final XLoginPlugin plugin;

    public AltAccountNotifer(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Schedules an alt account check for a player in an async thread. The check only takes into account accounts which
     * have been used in the last {@link #IGNORE_ALTS_OLDER_THAN_DAYS} days. If any alt accounts are encountered, a
     * message is broadcast to all players with the {@code xlogin.altinfo} permission. All accounts on one IP are
     * considered alt accounts.
     *
     * @param plr the player to check for alt accounts
     */
    public void scheduleCheck(AuthedPlayer plr) {
        scheduleCheck(plr, null);
    }

    /**
     * Schedules an alt account check for a player in an async thread. The check only takes into account accounts which
     * have been used in the last {@link #IGNORE_ALTS_OLDER_THAN_DAYS} days. If any alt accounts are encountered, a
     * message is broadcast to all players with the {@code xlogin.altinfo} permission. All accounts on one IP are
     * considered alt accounts. If there are no alt accounts, {@code receiver} is notified of that fact, if not null.
     *
     * @param plr      the player to check for alt accounts
     * @param receiver the receiver of result messages, or null to notify all players.
     */
    public void scheduleCheck(final AuthedPlayer plr, final CommandSender receiver) {
        Preconditions.checkNotNull(plr, "plr");
        if (plr.getLastIp() == null) {
            ProxiedPlayer proxiedPlayer = plugin.getProxy().getPlayer(plr.getUniqueId());
            if (proxiedPlayer == null) {
                return; //Not much we can do
            }
            plr.setLastIp(proxiedPlayer.getAddress().toString());
        }
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                List<AuthedPlayer> ipPlayers = Lists.newArrayList(
                        AuthedPlayerFactory.getByCriteria(plr.getLastIp(), plugin.getRepository())
                );

                Iterator<AuthedPlayer> it = ipPlayers.iterator();
                while (it.hasNext()) { //Remove players which haven't logged in during the last two weeks
                    AuthedPlayer currentPlayer = it.next();
                    if (currentPlayer.getUniqueId().equals(plr.getUniqueId()) || //they themselves are not an alt
                            currentPlayer.getLastLoginDate() == null) { //if they don't have a recorded login date, that's >14 days for sure
                        it.remove();
                        continue;
                    }
                    long msSinceLogin = System.currentTimeMillis() - currentPlayer.getLastLoginDate().getTime();
                    long daysSinceLogin = TimeUnit.MILLISECONDS.toDays(msSinceLogin);
                    if (daysSinceLogin > IGNORE_ALTS_OLDER_THAN_DAYS) { //ignore accounts used >14d ago
                        it.remove();
                    }
                }

                if (ipPlayers.isEmpty()) {
                    if (receiver != null) {
                        receiver.sendMessage(new XyComponentBuilder(plr.getName()).color(plr.getPremiumColor())
                                .append(" hat keine offensichtlichen Doppelaccounts.", ChatColor.GREEN)
                                .create());
                    }
                    return; //No recent alts on this account
                }

                XyComponentBuilder builder = plugin.getPrefix().append(plr.getName(), plr.getPremiumColor())
                        .event(plr.buildHoverInfo())
                        .command("/xlo user " + plr.getUniqueId())
                        .append(" hat Doppelaccounts: ", ChatColor.GRAY);

                boolean first = true;
                for (AuthedPlayer altPlayer : ipPlayers) {
                    if (!first) {
                        builder.append(", ", ChatColor.GRAY);
                    }
                    builder.append(altPlayer.getName(), altPlayer.getPremiumColor())
                            .event(altPlayer.buildHoverInfo());
                    first = false;
                }

                BaseComponent[] components = builder.create();

                for (ProxiedPlayer proxiedPlayer : plugin.getProxy().getPlayers()) { //Receiver must be in there - permission required to request
                    if (proxiedPlayer.hasPermission("xlogin.altinfo")) {
                        proxiedPlayer.sendMessage(components);
                    }
                }
            }
        });
    }
}
