package io.github.xxyy.xlogin.spigot.commands;

import io.github.xxyy.xlogin.spigot.XLoginPlugin;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 0 && args[0].equalsIgnoreCase("help")) {
            return false;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden!");
            return true;
        }

        ((Player) sender).teleport(plugin.getSpawnLocation());
        sender.sendMessage(plugin.getConfig().getString("messages.spawntp"));

        return true;
    }
}
