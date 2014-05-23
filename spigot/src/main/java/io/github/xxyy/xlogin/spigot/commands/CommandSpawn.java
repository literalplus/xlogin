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
        if(args.length > 0 && args[0].equalsIgnoreCase("help")) {
            return false;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden!");
            return true;
        }

        final Player plr = (Player) sender;

        if(plr.hasPermission("xlogin.notpdelay")){
            completeTp(plr);
        } else {
            plr.sendMessage(plugin.getConfig().getString("messages.spawndelay"));
            final Location oldLoc = plr.getLocation();
            final double oldHealth = plr.getHealth();
            final double oldAir = plr.getRemainingAir();

            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    String denyMsg = null;

                    if(plr.getLocation() != oldLoc) {
                        denyMsg = "tpdmove";
                    } else if (plr.getHealth() != oldHealth) {
                        denyMsg = "tpdhit";
                    } else if (plr.getRemainingAir() != oldAir) {
                        denyMsg = "tpdair";
                    }

                    if(denyMsg != null) {
                        plr.sendMessage(plugin.getConfig().getString("messages."+denyMsg));
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
