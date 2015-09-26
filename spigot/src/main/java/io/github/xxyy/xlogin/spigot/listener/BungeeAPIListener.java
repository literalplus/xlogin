package io.github.xxyy.xlogin.spigot.listener;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import io.github.xxyy.xlogin.common.Const;
import io.github.xxyy.xlogin.common.api.spigot.event.AuthenticationEvent;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.spigot.XLoginPlugin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class BungeeAPIListener implements PluginMessageListener {
    private final XLoginPlugin plugin;

    public BungeeAPIListener(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player plrReceiver, byte[] data) {
        ByteArrayInputStream bi = new ByteArrayInputStream(data);
        DataInputStream ds = new DataInputStream(bi);
        try {
            if (channel.equalsIgnoreCase(Const.API_CHANNEL_NAME)) {
                String command = ds.readUTF();
                if (command.equalsIgnoreCase("tp")) {
                    //Teleports the target player to their last notation - DEPRECATED: Now handled in auth
                } else if (command.equalsIgnoreCase("auth")) {
                    final UUID uuid = UUID.fromString(ds.readUTF());

                    final Player plr = Bukkit.getPlayer(uuid);
                    final AuthedPlayer.AuthenticationProvider authProvider =
                            AuthedPlayer.AuthenticationProvider.values()[ds.readInt()];

                    if (plr == null) {
                        plugin.getLogger().info(String.format(
                                "Received auth notification for unknown player {UUID=%s, AuthenticationProvider=%s}",
                                uuid, authProvider.name()));
                        return;
                    }

                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            AuthedPlayer authedPlayer = plugin.getRepository().refreshPlayer(uuid, plr.getName());
                            Preconditions.checkNotNull(authedPlayer, "authedPlayer");
                            authedPlayer.setAuthenticationProvider(authProvider);
                            authedPlayer.setAuthenticated(true);

                            plugin.getRegistry().registerAuthentication(authedPlayer);
                            plugin.getServer().getPluginManager().callEvent(new AuthenticationEvent(plr, authedPlayer));
                            plugin.getLogger().info(String.format("Received auth for %s w/ %s using %s",
                                    plr.getName(), uuid, authProvider.name()));
                            plugin.teleportToLastLocation(plr, 10L);
                        }
                    });
                } else if (command.equalsIgnoreCase("register")) {
                    UUID uuid = UUID.fromString(ds.readUTF());
                    Player plr = Bukkit.getPlayer(uuid);

                    if (plr == null) {
                        plugin.getLogger().info(String.format("Received register notification for unknown player {UUID=%s}",
                                uuid));
                        return;
                    }

                    plugin.getRepository().refreshPlayer(uuid, plr.getName());
                    plugin.getLogger().info(String.format("Received register for %s w/ %s", plr.getName(), uuid));
                } else if (command.equalsIgnoreCase("resend-ok")) {
                    GenericListener.skip = false;
                } else if (command.equals("server-name")) {
                    plugin.setServerName(ds.readUTF());
                    plugin.getLogger().info("Received server name: " + plugin.getServerName());
                } else {
                    plugin.getLogger().info("Received unknown API message with action=" + command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
