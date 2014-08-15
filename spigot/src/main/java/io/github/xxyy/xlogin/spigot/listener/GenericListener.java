package io.github.xxyy.xlogin.spigot.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.xxyy.xlogin.spigot.XLoginPlugin;


public class GenericListener implements Listener {
    public static boolean skip = false;
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

        plugin.saveLocation(plr, true);

        evt.setQuitMessage(null);

        plugin.getRegistry().forget(evt.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent evt) {
        plugin.saveLocation(evt.getPlayer(), true);

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
        if (skip) {
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
        if (skip) {
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
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        final Player plr = evt.getPlayer();
        evt.setJoinMessage(null); //TODO configurable

//        plugin.getRepository().isPlayerKnown(plr.getUniqueId()); //Pre-fetch

        plugin.getServer().getScheduler().runTaskLater(plugin,
                new Runnable() {
                    public void run() {
                        plr.teleport(plugin.getSpawnLocation());


                        if (plugin.getServerName() == null) { //Requesting in here because Bukkit is a bit weird with initializing its stuffs
                            plugin.sendAPIMessage(plr, "server-name", false); //Request server name - there might not have been any players online at startup
                        }
                    }
                }
                ,
                3L); //Let the player take their time to arrive - We have time! :)

    }
}
