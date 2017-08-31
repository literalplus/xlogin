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

package li.l1t.xlogin.bungee.listener;

import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.text.MessageFormat;

/**
 * Listens for extremely important events. Wow.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
public class MainListener implements Listener {
    private final XLoginPlugin plugin;

    public MainListener(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(final ChatEvent evt) {
        if (evt.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer plr = (ProxiedPlayer) evt.getSender();

            if (evt.getMessage().startsWith("/login")
                    || evt.getMessage().startsWith("/register")) {
                return;
            }

            if (!plugin.getRepository().isPlayerKnown(plr.getUniqueId())) {
                evt.setCancelled(true);

                plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notRegistered));
            } else if (!plugin.getRegistry().isAuthenticated(plr.getUniqueId())) {
                evt.setCancelled(true);

                plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));
            }
        }
    }

    @EventHandler
    public void onPluginMessage(final PluginMessageEvent evt) {
        if (evt.getTag().equals(XLoginPlugin.API_CHANNEL_NAME)) {
            if (evt.getSender() instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) evt.getSender();

                player.disconnect(new ComponentBuilder("youtried.png").color(ChatColor.DARK_RED).create());
                plugin.getLogger().warning(MessageFormat.format("Player {0} ({1}) tried to fake auth messages from IP {2}",
                        player.getName(), player.getUniqueId().toString(), player.getAddress().getAddress().toString()));
                return; //TODO: Maybe we should encrypt messages somehow
            }

            try (ByteArrayInputStream bi = new ByteArrayInputStream(evt.getData())) {
                try (DataInputStream ds = new DataInputStream(bi)) {
                    String command = ds.readUTF();

                    if (command.equals("resend")) {
                        Validate.validState(evt.getSender() instanceof Server, "Cannot receive resend from anything else than a server");
                        Server server = (Server) evt.getSender();
                        ProxyServer.getInstance().getLogger().info("Resending auth data to " + server.getInfo().getName() + "!");

                        for (ProxiedPlayer plr : server.getInfo().getPlayers()) {
                            AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());

                            if (authedPlayer.isAuthenticated()) {
                                plugin.sendAuthNotification(plr, authedPlayer); //not using #notifyAuthentication to prevent spam of alt account messages
                                plugin.teleportToLastLocation(plr);
                            }
                        }

                        plugin.sendAPIMessage(server, "resend-ok");
                    } else if (command.equals("server-name")) {
                        Validate.isTrue(evt.getSender() instanceof Server, "Invalid sender found for server-name plugin message");
                        Server server = (Server) evt.getSender();
                        plugin.getLogger().info("Sending server name to " + server.getInfo().getName());

                        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                            try (DataOutputStream dos = new DataOutputStream(bos)) {
                                dos.writeUTF("server-name");
                                dos.writeUTF(server.getInfo().getName());
                            } catch (IOException ignore) {
                                //go home BungeeCord, you have drunk
                            }

                            server.sendData(XLoginPlugin.API_CHANNEL_NAME, bos.toByteArray());
                        } catch (IOException ignore) {
                            //oke what you're gonna do tho
                        }
                    } else {
                        plugin.getLogger().info("Received unknown api message: " + command);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
