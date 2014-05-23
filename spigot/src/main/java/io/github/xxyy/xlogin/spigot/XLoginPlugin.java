package io.github.xxyy.xlogin.spigot;

import io.github.xxyy.common.util.LocationHelper;
import io.github.xxyy.xlogin.common.api.SpawnLocationHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;
import io.github.xxyy.xlogin.common.sql.EbeanManager;
import io.github.xxyy.xlogin.spigot.listener.BungeeAPIListener;
import io.github.xxyy.xlogin.spigot.listener.GenericListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents the main interface of the xLogin plugin for Spigot.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
public class XLoginPlugin extends JavaPlugin {
    public static final AuthedPlayerRegistry AUTHED_PLAYER_REGISTRY = new AuthedPlayerRegistry();
    public static final AuthedPlayerRepository AUTHED_PLAYER_REPOSITORY = new AuthedPlayerRepository();
    @Getter
    private Location spawnLocation;

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        //Register Bukkit stuffs
        BungeeAPIListener apiListener = new BungeeAPIListener(this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "xLo-BungeeAPI");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "xLo-BungeeAPI", apiListener);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "Authtopia", apiListener);
        Bukkit.getPluginManager().registerEvents(new GenericListener(this), this);

        //Register the database we stole from Bukkit with our common lib
        EbeanManager.setEbean(getDatabase());

        //Init config
        initConfig();
    }

    public void initConfig() {
        this.getConfig().options().copyHeader(true);
        this.getConfig().options().header("xLogin Spigot config.");
        this.getConfig().options().copyDefaults(true);
        this.getConfig().addDefault("spawn.x", 0);
        this.getConfig().addDefault("spawn.y", 0);
        this.getConfig().addDefault("spawn.z", 0);
        this.getConfig().addDefault("spawn.pitch", 0F);
        this.getConfig().addDefault("spawn.yaw", 0F);
        this.getConfig().addDefault("spawn.world", Bukkit.getWorlds().get(0));
        this.getConfig().addDefault("messages.spawntp", "§6Du wurdest zum Spawn teleportiert.");
        this.getConfig().addDefault("messages.notloggedin", "§6Du bist nicht eingeloggt! Versuche §e/login§6!");
        this.getConfig().addDefault("messages.notregistered", "§6Du bist nicht registriert! Versuche §e/register§6!");
        this.saveConfig();

        try {
            spawnLocation = LocationHelper.fromDetailedConfiguration(this.getConfig().getConfigurationSection("spawn"));
        } catch (IllegalArgumentException e) {
            spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            getLogger().warning("Unable to get spawn location! Using default spawn! Details: "+e.getMessage());
        }

        SpawnLocationHolder.setSpawn(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ(),
                spawnLocation.getPitch(), spawnLocation.getYaw(), spawnLocation.getWorld().getName());
    }

    public void teleportToLastLocation(Player plr) {
        AuthedPlayer authedPlayer = AUTHED_PLAYER_REPOSITORY.getPlayer(plr.getUniqueId(), plr.getName());

        int x = authedPlayer.getLastLogoutBlockX();
        int y = authedPlayer.getLastLogoutBlockY();
        int z = authedPlayer.getLastLogoutBlockZ();
        String worldName = authedPlayer.getLastWorldName();

        if(x == 0 || y == 0 || z == 0) {
            plr.teleport(spawnLocation);
        } else {
            World world = getServer().getWorld(authedPlayer.getLastWorldName());
            if(world == null) {
                world = spawnLocation.getWorld();
            }

            plr.teleport(new Location(world, authedPlayer.getLastLogoutBlockX(),
                    authedPlayer.getLastLogoutBlockY(), authedPlayer.getLastLogoutBlockZ()));
        }
    }
}
