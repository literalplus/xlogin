/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.l1t.xlogin.spigot.commands;

import li.l1t.xlogin.spigot.XLoginPlugin;
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
