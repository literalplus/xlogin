package io.github.xxyy.xlogin.spigot.listener;

import io.github.xxyy.xlogin.spigot.XLoginPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduer.BukkitScheduler;


public class GenericListener implements Listener {
    private final String notLoggedInMessage;
    private final String notRegisteredMessage;
    private final XLoginPlugin plugin;
    public static boolean skip = false;

    public GenericListener(XLoginPlugin plugin) {
        this.plugin = plugin;
        notLoggedInMessage = plugin.getConfig().getString("messages.notloggedin");
        notRegisteredMessage = plugin.getConfig().getString("messages.notregistered");
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(final PlayerQuitEvent evt) {
        Player plr = evt.getPlayer();

        plugin.saveLocation(plr);

        evt.setQuitMessage(null);

        plugin.getRegistry().forget(evt.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent evt) {
        plugin.saveLocation(evt.getPlayer());

        plugin.getRegistry().forget(evt.getPlayer().getUniqueId());
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
        if(skip){
            return;
        }

        Player plr = evt.getPlayer();
        if (!plugin.getRepository().isPlayerKnown(plr.getUniqueId())) {
            if (((evt.getTo().getX() != evt.getFrom().getX()) || (evt.getTo().getZ() != evt.getFrom().getZ()))) {
                plr.sendMessage(this.notRegisteredMessage); //Only message when actually moving - microopt
                evt.setTo(evt.getFrom());
                return;
            }
            return;
        }

        if (!plugin.getRegistry().isAuthenticated(plr.getUniqueId())) {
            if (((evt.getTo().getX() != evt.getFrom().getX()) || (evt.getTo().getZ() != evt.getFrom().getZ())) || (evt.getTo().getY() != evt.getFrom().getY())) {
                plr.sendMessage(this.notLoggedInMessage);
                evt.setTo(evt.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent evt) {
        checkLoginAndMsg(evt, evt.getPlayer());
    }

    @EventHandler
    public void onDmg(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        checkLoginAndMsg(e, (Player) e.getEntity());
    }

    public void checkLoginAndMsg(Cancellable e, Player plr) {
        if(skip) {
            return;
        }

        if (!plugin.getRepository().isPlayerKnown(plr.getUniqueId())) {
            plr.sendMessage(this.notRegisteredMessage);

            e.setCancelled(true);
            return;
        }

        if (!plugin.getRegistry().isAuthenticated(plr.getUniqueId())) {
            plr.sendMessage(this.notLoggedInMessage);

            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        final Player plr = evt.getPlayer();
        evt.setJoinMessage(null); //TODO configurable

        plugin.getRepository().isPlayerKnown(plr.getUniqueId()); //Pre-fetch

        plugin.getServer().getScheduler().runTaskLater(plugin, 
            () -> plr.teleport(plugin.getSpawnLocation(),
            10L); //Let the player take their time to arrive - We have time! :)
        //evt.getPlayer().teleport(plugin.getSpawnLocation()); //Uncomment if spawning lags too much
    }
}
