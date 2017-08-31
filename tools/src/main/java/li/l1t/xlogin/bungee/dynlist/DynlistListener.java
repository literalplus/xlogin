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

package li.l1t.xlogin.bungee.dynlist;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Listens for events related to Dynlist, xLogin's dynamic whitelist system.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28/12/14
 */
public class DynlistListener implements Listener {
    private final DynlistModule manager;
    private boolean fullBypass = false;
    private Map<UUID, String> bypasses = new HashMap<>();

    public DynlistListener(DynlistModule manager) {
        this.manager = manager;
    }

    @EventHandler //Summon compatibility (/send)
    public void onCommand(ChatEvent evt) {
        if (!evt.getMessage().startsWith("/send ")) {
            return;
        }

        String[] args = evt.getMessage().split(" ");
        if (args.length != 2) {
            return;
        }

        if (!isBlocked(args[2], null)) {
            return;
        }

        if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("current")) {
            manager.getPlugin().getProxy().getScheduler().schedule(manager.getPlugin(),
                    new Runnable() {
                        @Override
                        public void run() {
                            fullBypass = false;
                        }
                    }, 1, TimeUnit.SECONDS);
            fullBypass = true;
            if (evt.getSender() instanceof CommandSender) {
                ((CommandSender) evt.getSender()).sendMessage("§aBypassing Dynlist for your /send command!");
            }
        } else {
            final ProxiedPlayer plr = manager.getPlugin().getProxy().getPlayer(args[1]);
            if (plr != null) {
                bypasses.put(plr.getUniqueId(), args[2]);
                manager.getPlugin().getProxy().getScheduler().schedule(manager.getPlugin(),
                        new Runnable() {
                            @Override
                            public void run() {
                                bypasses.remove(plr.getUniqueId());
                            }
                        }, 1, TimeUnit.SECONDS);
            }
            if (evt.getSender() instanceof CommandSender) {
                ((CommandSender) evt.getSender()).sendMessage("§aBypassing Dynlist for your /send command!");
            }
        }
    }

    @EventHandler
    public void onServerSwitch(ServerConnectEvent evt) {
        if (evt.isCancelled() || evt.getPlayer().hasPermission("xlogin.dynlist") || fullBypass) {
            return;
        }

        if (bypasses.containsKey(evt.getPlayer().getUniqueId()) &&
                evt.getTarget().getName().equalsIgnoreCase(bypasses.get(evt.getPlayer().getUniqueId()))) {
            bypasses.remove(evt.getPlayer().getUniqueId());
        }

        if (isBlocked(evt.getTarget(), evt.getPlayer())) {
            evt.getPlayer().sendMessage(new ComponentBuilder("Du darfst diesen Server momentan nicht betreten!").color(ChatColor.RED).create());
            sendToFallbackServer(evt);
            evt.setCancelled(true); //We reconnect so that we can watch for errors while connecting
        }
    }

    public boolean isBlocked(ServerInfo target, ProxiedPlayer player) {
        return isBlocked(target.getName(), player);
    }

    public boolean isBlocked(String targetName, ProxiedPlayer player) {
        List<DynlistEntry> matches = manager.getMatches(targetName);

        if (player == null) {
            return !matches.isEmpty();
        }

        for (DynlistEntry match : matches) {
            if (!player.hasPermission("xlogin.dlby." + match.getName())) {
                return true;
            }
        }

        return false;
    }

    private boolean sendToFallbackServer(final ServerConnectEvent evt) {
        String fallbackServerName = evt.getPlayer().getPendingConnection().getListener().getFallbackServer();
        if (evt.getTarget().getName().equalsIgnoreCase(fallbackServerName)) {
            if (evt.getPlayer().getServer() == null) { //Initial join
                evt.getPlayer().disconnect(
                        new ComponentBuilder("MinoTopia wird gerade gewartet. Bitte versuche es später erneut. (1)").color(ChatColor.RED).create()
                );
            }
            return false;
        }

        if (evt.getPlayer().getServer() != null && evt.getPlayer().getServer().getInfo().getName().equals(fallbackServerName)) {
            return true;
        }

        ServerInfo info = manager.getPlugin().getProxy().getServerInfo(fallbackServerName);
        if (info == null) {
            evt.getPlayer().disconnect(
                    new ComponentBuilder("Unbekannter Ersatzserver. Bitte versuche es später erneut.").color(ChatColor.RED).create()
            );
            return false;
        }

        evt.getPlayer().connect(info, new Callback<Boolean>() {
            @Override
            public void done(Boolean result, Throwable error) {
                if (!result || error != null) {
                    evt.getPlayer().disconnect(
                            new ComponentBuilder("MinoTopia wird gerade gewartet. Bitte versuche es später erneut. (2)").color(ChatColor.RED).create()
                    );
                }
            }
        });
        return true;
    }
}
