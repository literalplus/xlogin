package io.github.xxyy.xlogin.bungee.dynlist;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

/**
 * Listens for events related to Dynlist, xLogin's dynamic whitelist system.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28/12/14
 */
public class DynlistListener implements Listener {
    private final DynlistModule manager;

    public DynlistListener(DynlistModule manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onServerSwitch(ServerConnectEvent evt) {
        if(evt.getPlayer().hasPermission("xlogin.dynlist")) {
            return;
        }

        List<DynlistEntry> matches = manager.getMatches(evt.getTarget());
        if (!matches.isEmpty()) {
            for (DynlistEntry match : matches) {
                if (!evt.getPlayer().hasPermission("xlogin.dlby." + match.getName())) {
                    evt.getPlayer().sendMessage(new ComponentBuilder("Du darfst diesen Server momentan nicht betreten!").color(ChatColor.RED).create());
                    evt.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent evt) {
        if(evt.getPlayer().hasPermission("xlogin.dynlist")) {
            return;
        }

        List<DynlistEntry> matches = manager.getMatches(evt.getPlayer().getServer().getInfo());
        if (matches.isEmpty()) {
            return;
        }

        for (DynlistEntry match : matches) {
            if (evt.getPlayer().hasPermission("xlogin.dlby." + match.getName())) {
                continue;
            }

            evt.getPlayer().sendMessage(new ComponentBuilder("Du darfst diesen Server momentan nicht betreten!").color(ChatColor.RED).create());
            String fallbackServerName = evt.getPlayer().getPendingConnection().getListener().getFallbackServer();
            if (evt.getPlayer().getServer().getInfo().getName().equalsIgnoreCase(fallbackServerName)) {
                evt.getPlayer().disconnect(
                        new ComponentBuilder("MinoTopia wird gerade gewartet. Bitte versuche es später erneut. (1)").color(ChatColor.RED).create()
                );
                return;
            }
            ServerInfo info = manager.getPlugin().getProxy().getServerInfo(fallbackServerName);
            if(info == null) {
                evt.getPlayer().disconnect(
                        new ComponentBuilder("Unbekannter Ersatzserver. Bitte versuche es später erneut.").color(ChatColor.RED).create()
                );
                return;
            }

            evt.getPlayer().connect(info, new Callback<Boolean>() {
                @Override
                public void done(Boolean result, Throwable error) {
                    if(!result || error != null) {
                        evt.getPlayer().disconnect(
                                new ComponentBuilder("MinoTopia wird gerade gewartet. Bitte versuche es später erneut. (2)").color(ChatColor.RED).create()
                        );
                    }
                }
            });
            return;
        }
    }
}
