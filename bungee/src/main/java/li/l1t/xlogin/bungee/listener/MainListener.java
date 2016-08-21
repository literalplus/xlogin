/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
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
