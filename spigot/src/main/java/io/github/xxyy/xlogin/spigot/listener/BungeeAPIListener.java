package io.github.xxyy.xlogin.spigot.listener;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.spigot.XLoginPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

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
            if (channel.equalsIgnoreCase("xLo-BungeeAPI")) {
                String command = ds.readUTF();
                if (command.equalsIgnoreCase("tp")) { //Teleports the target player to their last notation
                    UUID uuid = UUID.fromString(ds.readUTF());

                    Player plr = Bukkit.getPlayer(uuid);

                    if (plr == null) {
                        plugin.getLogger().info(MessageFormat.format("Received tp request for unknown player '{'UUID={0}}'",
                                uuid));
                        return;
                    }

                    plugin.teleportToLastLocation(plr);
                } else if (command.equalsIgnoreCase("auth")) {
                    UUID uuid = UUID.fromString(ds.readUTF());
                    AuthedPlayer.AuthenticationProvider authProvider = AuthedPlayer.AuthenticationProvider.values()[ds.readInt()];

                    Player plr = Bukkit.getPlayer(uuid);

                    if (plr == null) {
                        plugin.getLogger().info(MessageFormat.format("Received auth notification for unknown player '{'UUID={0}, AuthenticationProvider={1}'}'",
                                uuid, authProvider.name()));
                        return;
                    }

                    XLoginPlugin.AUTHED_PLAYER_REGISTRY.registerAuthentication(XLoginPlugin.AUTHED_PLAYER_REPOSITORY.getPlayer(uuid, plr.getName()));
                    plugin.getLogger().info(MessageFormat.format("Received auth for {0} w/ {1} using {2}", plr.getName(), uuid, authProvider.name()));
                } else if (command.equalsIgnoreCase("register")) {
                    UUID uuid = UUID.fromString(ds.readUTF());

                    XLoginPlugin.AUTHED_PLAYER_REPOSITORY.updateKnown(uuid, true);
                } else {
                    plugin.getLogger().info("Received unknown API message with action=" + command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
