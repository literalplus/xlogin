package io.github.xxyy.xlogin.spigot.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import io.github.xxyy.xlogin.common.Const;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.spigot.XLoginPlugin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.MessageFormat;
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
                if (command.equalsIgnoreCase("tp")) { //Teleports the target player to their last notation
                    UUID uuid = UUID.fromString(ds.readUTF());

                    Player plr = Bukkit.getPlayer(uuid);

                    if (plr == null) {
                        plugin.getLogger().info(MessageFormat.format("Received tp request for unknown player '{'UUID={0}'}'",
                                uuid));
                        return;
                    }

                    plugin.teleportToLastLocation(plr);
                } else if (command.equalsIgnoreCase("auth")) {
                    UUID uuid = UUID.fromString(ds.readUTF());

                    Player plr = Bukkit.getPlayer(uuid);
                    AuthedPlayer.AuthenticationProvider authProvider = AuthedPlayer.AuthenticationProvider.values()[ds.readInt()];

                    if (plr == null) {
                        plugin.getLogger().info(MessageFormat.format("Received auth notification for unknown player '{'UUID={0}, AuthenticationProvider={1}'}'",
                                uuid, authProvider.name()));
                        return;
                    }

                    AuthedPlayer authedPlayer = plugin.getRepository().refreshPlayer(uuid, plr.getName());
                    authedPlayer.setAuthenticationProvider(authProvider);
                    authedPlayer.setAuthenticated(true);


                    plugin.getRegistry().registerAuthentication(authedPlayer);
                    plugin.getLogger().info(MessageFormat.format("Received auth for {0} w/ {1} using {2}", plr.getName(), uuid, authProvider.name()));
                } else if (command.equalsIgnoreCase("register")) {
                    UUID uuid = UUID.fromString(ds.readUTF());
                    Player plr = Bukkit.getPlayer(uuid);

                    if (plr == null) {
                        plugin.getLogger().info(MessageFormat.format("Received register notification for unknown player '{'UUID={0}'}'",
                                uuid));
                        return;
                    }

                    plugin.getRepository().refreshPlayer(uuid, plr.getName());
                    plugin.getLogger().info(MessageFormat.format("Received register for {0} w/ {1}", plr.getName(), uuid));
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
