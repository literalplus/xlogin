package io.github.xxyy.xlogin.bungee.listener;

import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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

            if(evt.getMessage().startsWith("/login")
                    || evt.getMessage().startsWith("/register")) {
                return;
            }

            if(!XLoginPlugin.AUTHED_PLAYER_REPOSITORY.isPlayerKnown(plr.getUniqueId())) {
                evt.setCancelled(true);

                plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notRegistered));
                return;
            }

            if (!XLoginPlugin.AUTHED_PLAYER_REGISTRY.isAuthenticated(plr.getUniqueId())) {
                evt.setCancelled(true);

                plr.sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));
            }
        }
    }
}
