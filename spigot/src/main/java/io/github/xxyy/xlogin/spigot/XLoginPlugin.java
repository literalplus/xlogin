package io.github.xxyy.xlogin.spigot;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.xxyy.common.shared.uuid.UUIDRepositories;
import io.github.xxyy.common.sql.SafeSql;
import io.github.xxyy.common.sql.SqlConnectables;
import io.github.xxyy.common.util.LocationHelper;
import io.github.xxyy.common.version.PluginVersion;
import io.github.xxyy.xlogin.common.Const;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.api.ApiConsumer;
import io.github.xxyy.xlogin.common.api.SpawnLocationHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;
import io.github.xxyy.xlogin.common.authedplayer.LocationInfo;
import io.github.xxyy.xlogin.spigot.commands.CommandLocalXLo;
import io.github.xxyy.xlogin.spigot.commands.CommandSpawn;
import io.github.xxyy.xlogin.spigot.listener.BungeeAPIListener;
import io.github.xxyy.xlogin.spigot.listener.GenericListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Represents the main interface of the xLogin plugin for Spigot.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
public class XLoginPlugin extends JavaPlugin implements ApiConsumer {
    public static final AuthedPlayerRepository AUTHED_PLAYER_REPOSITORY = new AuthedPlayerRepository();
    public static final AuthedPlayerRegistry AUTHED_PLAYER_REGISTRY = new AuthedPlayerRegistry(AUTHED_PLAYER_REPOSITORY);
    public static final String API_CHANNEL_NAME = Const.API_CHANNEL_NAME;
    @Getter
    private Location spawnLocation;
    @Getter
    private String serverName;

    @Override
    public void onDisable() {
        for (Player plr : Bukkit.getOnlinePlayers()) {
            saveLocation(plr, false); //We can't register async tasks when (being) disabled :/
        }
    }

    @Override
    public void onEnable() {
        //Register Bukkit stuffs
        BungeeAPIListener apiListener = new BungeeAPIListener(this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, API_CHANNEL_NAME);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, API_CHANNEL_NAME, apiListener);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "Authtopia", apiListener);
        Bukkit.getPluginManager().registerEvents(new GenericListener(this), this);
        getCommand("spawn").setExecutor(new CommandSpawn(this));
        getCommand("lxlo").setExecutor(new CommandLocalXLo(this));

        //Init config
        initConfig();

        //Init database connection
        String dbName = getConfig().getString("sql.db");
        PreferencesHolder.setSql(new SafeSql(SqlConnectables.fromCredentials(
                SqlConnectables.getHostString(dbName, getConfig().getString("sql.host")),
                dbName,
                getConfig().getString("sql.user"),
                getConfig().getString("sql.password"))));


        if (Bukkit.getOnlinePlayers().length > 0) {
            sendAPIMessage(Bukkit.getOnlinePlayers()[0], "resend", false);
            sendAPIMessage(Bukkit.getOnlinePlayers()[0], "server-name", false);
            GenericListener.skip = true;
        }

        //Register api stuffs
        PreferencesHolder.setConsumer(this);

        //Register XYC uuid provider
        UUIDRepositories.addRepository(AUTHED_PLAYER_REPOSITORY, this);

        //Register task saving locations every 5m
        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (Player plr : Bukkit.getOnlinePlayers()) {
                    saveLocation(plr, true);
                }
            }
        }, 20 * 60 * 5L, 20 * 60 * 5L);

        getLogger().info("xLogin " + PluginVersion.ofClass(getClass()) + " enabled!");
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
        this.getConfig().addDefault("spawn.world", Bukkit.getWorlds().get(0).getName());
        this.getConfig().addDefault("messages.spawntp", "§6Du wurdest zum Spawn teleportiert.");
        this.getConfig().addDefault("messages.notloggedin", "§6Du bist nicht eingeloggt! Versuche §e/login§6!");
        this.getConfig().addDefault("messages.notregistered", "§6Du bist nicht registriert! Versuche §e/register§6!");
        this.getConfig().addDefault("messages.spawndelay", "§6Du wirst in 2 Sekunden teleportiert. Bewege dich nicht!");
        this.getConfig().addDefault("messages.tpdmove", "§cDu hast dich bewegt!");
        this.getConfig().addDefault("messages.tpdhit", "§cDu hast Schaden genommen!");
        this.getConfig().addDefault("messages.tpdair", "§cTrockne dich zuerst ab, bevor zu zum Spawn gehst ;)");
        this.getConfig().addDefault("sql.user", "bungeecord");
        this.getConfig().addDefault("sql.db", "bungeecord");
        this.getConfig().addDefault("sql.password", "");
        this.getConfig().addDefault("sql.host", "jdbc:mysql://localhost:3306/bungeecord");
        this.saveConfig();

        spawnLocation = null;
        try {
            updateSpawnLocation(LocationHelper.fromDetailedConfiguration(this.getConfig().getConfigurationSection("spawn")));
        } catch (IllegalArgumentException e) {
            getLogger().warning("Unable to get spawn location! Using default spawn! Details: " + e.getMessage());
        }

        if (spawnLocation == null) {
            getLogger().warning("Couldn't load spawn.");
            updateSpawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        serverName = getConfig().getString("server-name");

        if (serverName == null) {
            getLogger().info("No server name given! Locations will be saved once BungeeCord responds to our query.");
        }
    }

    public void updateSpawnLocation(Location location) {
        this.spawnLocation = location;

        SpawnLocationHolder.setSpawn(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ(),
                spawnLocation.getPitch(), spawnLocation.getYaw(), spawnLocation.getWorld().getName());


        spawnLocation.getWorld().setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
    }

    public void teleportToLastLocation(Player plr) {
        AuthedPlayer authedPlayer = AUTHED_PLAYER_REPOSITORY.getProfile(plr.getUniqueId());

        if (getServerName() == null) {
            getLogger().severe("No server name!");
            plr.sendMessage("§c§lInterner Fehler: Kein Servername bekannt! Du konntest nicht zurückteleportiert werden!");
            return;
        }

        LocationInfo lastLocation = authedPlayer.getLastLocation(getServerName());

        if (lastLocation == null) {
            plr.teleport(spawnLocation);
            getLogger().info("No previous location for " + plr.getName());
        } else {
            World world = getServer().getWorld(lastLocation.getWorldName());
            if (world == null) {
                world = spawnLocation.getWorld();
            }

            plr.teleport(new Location(world, lastLocation.getX(),
                    lastLocation.getY(), lastLocation.getZ()));
        }
    }

    public void sendAPIMessage(Player plr, String action, boolean sendUUID) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (DataOutputStream dos = new DataOutputStream(bos)) {
                dos.writeUTF(action);
                if (sendUUID) {
                    dos.writeUTF(plr.getUniqueId().toString());
                }
            } catch (IOException ignore) {
                //go home Spigot, you have drunk
            }

            plr.sendPluginMessage(this, API_CHANNEL_NAME, bos.toByteArray());
        } catch (IOException ignore) {
            //oke what you're gonna do tho
        }
    }

    public void saveLocation(Player plr, boolean async) {
        final UUID uuid = plr.getUniqueId();
        final Location loc = plr.getLocation();

        Runnable saveLogic = new Runnable() {
            @Override
            public void run() {
                AuthedPlayer authedPlayer = AUTHED_PLAYER_REPOSITORY.getProfile(uuid);

                if (authedPlayer == null) {
                    return;
                }

                authedPlayer.setLastLocation(getServerName(), loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
            }
        };

        if (async) {
            getServer().getScheduler().runTaskAsynchronously(this, saveLogic);
        } else {
            saveLogic.run();
        }
    }

    public void setSpawn(Location location) {
        updateSpawnLocation(location);

        this.getConfig().set("spawn.x", location.getBlockX());
        this.getConfig().set("spawn.y", location.getBlockY());
        this.getConfig().set("spawn.z", location.getBlockZ());
        this.getConfig().set("spawn.pitch", location.getPitch());
        this.getConfig().set("spawn.yaw", location.getYaw());
        this.getConfig().set("spawn.world", location.getWorld().getName());
        saveConfig();
    }

    @Override
    public AuthedPlayerRepository getRepository() {
        return AUTHED_PLAYER_REPOSITORY;
    }

    @Override
    public AuthedPlayerRegistry getRegistry() {
        return AUTHED_PLAYER_REGISTRY;
    }

    public void setServerName(String newServerName) {
        serverName = newServerName;
        this.getConfig().set("server-name", serverName);
        saveConfig();
    }
}
