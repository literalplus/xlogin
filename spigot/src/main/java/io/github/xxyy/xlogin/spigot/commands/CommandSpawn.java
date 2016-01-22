/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.spigot.commands;

import io.github.xxyy.xlogin.spigot.XLoginPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Teleports players to spawn. wow.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
public class CommandSpawn implements CommandExecutor {
    private final XLoginPlugin plugin;

    public CommandSpawn(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if(!plugin.isSpawnEnabled()) {
            sender.sendMessage("§c/spawn ist auf diesem Server nicht erlaubt.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden!");
            return true;
        }

        final Player plr = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
            if (plr.hasPermission("xlogin.admin")) {
                if (args.length < 2 || !args[1].equalsIgnoreCase("sicher")) {
                    plr.sendMessage("§4Sicher? /spawn set sicher");
                    return true;
                } else {
                    plugin.setSpawn(plr.getLocation());
                    plr.sendMessage("§6Neuer Spawn: Deine Position (" + plr.getLocation() + ")");
                    return true;
                }
            } else {
                plr.sendMessage("§4403 Permishun is denie §7(dis is rhyme if u pronounce number german lole)");
                return true;
            }
        }

        if (plr.hasPermission("xlogin.notpdelay")) {
            completeTp(plr);
        } else {
            plr.sendMessage(plugin.getConfig().getString("messages.spawndelay"));
            final Location oldLoc = plr.getLocation();
            final double oldHealth = plr.getHealth();
            final double oldAir = plr.getRemainingAir();

            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    String denyMsg = null;

                    if (plr.getLocation().getBlockX() != oldLoc.getBlockX() ||
                            plr.getLocation().getBlockY() != oldLoc.getBlockY() ||
                            plr.getLocation().getBlockZ() != oldLoc.getBlockZ()) {
                        denyMsg = "tpdmove";
                    } else if (plr.getHealth() != oldHealth) {
                        denyMsg = "tpdhit";
                    } else if (plr.getRemainingAir() != oldAir) {
                        denyMsg = "tpdair";
                    }

                    if (denyMsg != null) {
                        plr.sendMessage(plugin.getConfig().getString("messages." + denyMsg));
                        return;
                    }

                    completeTp(plr);
                }
            }, 2 * 20L);
        }

        return true;
    }

    protected void completeTp(Player plr) {
        plr.teleport(plugin.getSpawnLocation());
        plr.sendMessage(plugin.getConfig().getString("messages.spawntp"));
    }
}
