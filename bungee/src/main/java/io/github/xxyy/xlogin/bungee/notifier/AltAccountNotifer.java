package io.github.xxyy.xlogin.bungee.notifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Notifies administrators if a users is using multiple names on a single IP address.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-22
 */
public class AltAccountNotifer {
    private static final long IGNORE_ALTS_OLDER_THAN_DAYS = 14;
    private final XLoginPlugin plugin;

    public AltAccountNotifer(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    public void scheduleCheck(final AuthedPlayer plr) {
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
                    return; //No recent alts on this account
                }

                XyComponentBuilder builder = plugin.getPrefix().append(plr.getName(), plr.getPremiumColor())
                        .event(plr.buildHoverInfo())
                        .command("/xlo user " + plr.getUniqueId())
                        .append(" hat Doppelaccounts: ", ChatColor.GRAY);

                boolean first = true;
                for (AuthedPlayer altPlayer : ipPlayers) {
                    builder.append(altPlayer.getName(), plr.getPremiumColor())
                            .event(altPlayer.buildHoverInfo());
                    if (first) {
                        builder.append(", ", ChatColor.GRAY);
                        first = false;
                    }
                }

                BaseComponent[] components = builder.create();

                for (ProxiedPlayer proxiedPlayer : plugin.getProxy().getPlayers()) {
                    if (proxiedPlayer.hasPermission("xlogin.altinfo")) {
                        proxiedPlayer.sendMessage(components);
                    }
                }
            }
        });
    }
}
