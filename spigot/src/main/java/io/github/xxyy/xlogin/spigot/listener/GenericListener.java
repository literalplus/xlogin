package io.github.xxyy.xlogin.spigot.listener;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import io.github.xxyy.xlogin.spigot.XLoginPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;


public class GenericListener implements Listener {
    private final String notLoggedInMessage;
    private final String notRegisteredMessage;
    private final XLoginPlugin plugin;

    public GenericListener(XLoginPlugin plugin) {
        this.plugin = plugin;
        notLoggedInMessage = plugin.getConfig().getString("messages.notloggedin");
        notRegisteredMessage = plugin.getConfig().getString("messages.notregistered");
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(final PlayerQuitEvent evt) {
        Player plr = evt.getPlayer();

        AuthedPlayer authedPlayer = XLoginPlugin.AUTHED_PLAYER_REPOSITORY.getPlayer(plr.getUniqueId(), plr.getName());

        if (authedPlayer != null) {
            authedPlayer.setLastLogoutBlockX(plr.getLocation().getBlockX());
            authedPlayer.setLastLogoutBlockY(plr.getLocation().getBlockY());
            authedPlayer.setLastLogoutBlockZ(plr.getLocation().getBlockZ());
            authedPlayer.setLastWorldName(plr.getLocation().getWorld().getName());
        }

        evt.setQuitMessage(null);

        AuthedPlayerFactory.remove(plr.getUniqueId());
        XLoginPlugin.AUTHED_PLAYER_REGISTRY.remove(evt.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        checkLoginAndMsg(e, e.getPlayer());
    }

    @EventHandler
    public void onInvDrop(PlayerDropItemEvent e) {
        checkLoginAndMsg(e, e.getPlayer());
    }

    @EventHandler
    public void onInvPickup(PlayerPickupItemEvent e) {
        checkLoginAndMsg(e, e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent evt) {
        Player plr = evt.getPlayer();
        if (!XLoginPlugin.AUTHED_PLAYER_REPOSITORY.isPlayerKnown(plr.getUniqueId())) {
            if (((evt.getTo().getX() != evt.getFrom().getX()) || (evt.getTo().getZ() != evt.getFrom().getZ()))) {
                plr.sendMessage(this.notRegisteredMessage); //Only message when actually moving - microopt
                evt.setTo(evt.getFrom());
                return;
            }
            return;
        }

        if (!XLoginPlugin.AUTHED_PLAYER_REGISTRY.isAuthenticated(plr.getUniqueId())) {
            if (((evt.getTo().getX() != evt.getFrom().getX()) || (evt.getTo().getZ() != evt.getFrom().getZ())) || (evt.getTo().getY() != evt.getFrom().getY())) {
                plr.sendMessage(this.notLoggedInMessage);
                evt.setTo(evt.getFrom());
            }
        }
    }

    @EventHandler
    public void onDmg(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        checkLoginAndMsg(e, (Player) e.getEntity());
    }

    public void checkLoginAndMsg(Cancellable e, Player plr) {
        if (!XLoginPlugin.AUTHED_PLAYER_REPOSITORY.isPlayerKnown(plr.getUniqueId())) {
            plr.sendMessage(this.notRegisteredMessage);

            e.setCancelled(true);
            return;
        }

        if (!XLoginPlugin.AUTHED_PLAYER_REGISTRY.isAuthenticated(plr.getUniqueId())) {
            plr.sendMessage(this.notLoggedInMessage);

            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        evt.setJoinMessage(null); //TODO configurable

        XLoginPlugin.AUTHED_PLAYER_REPOSITORY.isPlayerKnown(evt.getPlayer().getUniqueId()); //Pre-fetch

        evt.getPlayer().teleport(plugin.getSpawnLocation());
    }
}
