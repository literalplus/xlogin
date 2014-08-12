package io.github.xxyy.xlogin.bungee.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.Validate;

import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
            try (ByteArrayInputStream bi = new ByteArrayInputStream(evt.getData())) {
                try (DataInputStream ds = new DataInputStream(bi)) {
                    String command = ds.readUTF();

                    if (command.equals("resend")) {
                        ProxyServer.getInstance().getLogger().info("Resending auth data!");
                        ProxiedPlayer finalPlr = null;

                        for (ProxiedPlayer plr : ProxyServer.getInstance().getPlayers()) {
                            AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());

                            if (authedPlayer.isAuthenticated()) {
                                plugin.sendAuthNotification(plr, authedPlayer);
                                plugin.teleportToLastLocation(plr);
                            }

                            finalPlr = plr;
                        }

                        plugin.sendAPIMessage(finalPlr, "resend-ok");
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
