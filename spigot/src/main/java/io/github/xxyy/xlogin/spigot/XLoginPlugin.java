package io.github.xxyy.xlogin.spigot;

import io.github.xxyy.common.sql.SafeSql;
import io.github.xxyy.common.sql.SqlConnectables;
import io.github.xxyy.common.util.LocationHelper;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.api.SpawnLocationHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;
import io.github.xxyy.xlogin.spigot.commands.CommandSpawn;
import io.github.xxyy.xlogin.spigot.listener.BungeeAPIListener;
import io.github.xxyy.xlogin.spigot.listener.GenericListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
public class XLoginPlugin extends JavaPlugin {
    public static final AuthedPlayerRegistry AUTHED_PLAYER_REGISTRY = new AuthedPlayerRegistry();
    public static final AuthedPlayerRepository AUTHED_PLAYER_REPOSITORY = new AuthedPlayerRepository();
    public static final String API_CHANNEL_NAME = "xLo-BungeeAPI";
    @Getter
    private Location spawnLocation;

//    @Override
//    public List<Class<?>> getDatabaseClasses() {
//        List<Class<?>> list = new ArrayList<>();
//        list.add(IpAddress.class);
//        list.add(AuthedPlayer.class);
//        list.add(Session.class);
//        list.add(FailedLoginAttempt.class);
//        return list;
//    }

    @Override
    public void onDisable() {
        for (Player plr : Bukkit.getOnlinePlayers()) {
            saveLocation(plr);
        }
    }

    @Override
    public void onEnable() {
        //Register Bukkit stuffs
        BungeeAPIListener apiListener = new BungeeAPIListener(this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, API_CHANNEL_NAME);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "xLo-BungeeAPI", apiListener);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "Authtopia", apiListener);
        Bukkit.getPluginManager().registerEvents(new GenericListener(this), this);
        getCommand("spawn").setExecutor(new CommandSpawn(this));

        //Register the database we stole from Bukkit with our common lib
//        EbeanManager.setEbean(getDatabase());
        PreferencesHolder.sql = new SafeSql(SqlConnectables.fromCredentials("jdbc:mysql://localhost:3306/mt_main", "mt_main",
                "bungeecord", "coH6eZjndMsZhXWggff4jLICQDuLEx1dJYp3ahOjmLrSJiWpaqXo8abnaneKahfRj1jaI5ZU787Le8sfwvBm2DvjAqAGV8Lez1Ps"));

        //Init config
        initConfig();

        if (Bukkit.getOnlinePlayers().length > 0) {
            sendAPIMessage(Bukkit.getOnlinePlayers()[0], "resend");
            GenericListener.skip = true;
        }
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
        this.getConfig().addDefault("sql.host", "jdbc://mysql:localhost:3306/bungeecord");
        this.saveConfig();

        try {
            spawnLocation = LocationHelper.fromDetailedConfiguration(this.getConfig().getConfigurationSection("spawn"));
        } catch (IllegalArgumentException e) {
            spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            getLogger().warning("Unable to get spawn location! Using default spawn! Details: " + e.getMessage());
        }

        if (spawnLocation == null) {
            getLogger().warning("Couldn't load spawn.");
            spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        }

        spawnLocation.getWorld().setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());

        SpawnLocationHolder.setSpawn(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ(),
                spawnLocation.getPitch(), spawnLocation.getYaw(), spawnLocation.getWorld().getName());
    }

    public void teleportToLastLocation(Player plr) {
        AuthedPlayer authedPlayer = AUTHED_PLAYER_REPOSITORY.getPlayer(plr.getUniqueId(), plr.getName());

        int x = authedPlayer.getLastLogoutBlockX();
        int y = authedPlayer.getLastLogoutBlockY();
        int z = authedPlayer.getLastLogoutBlockZ();
        String worldName = authedPlayer.getLastWorldName();

        if (x == 0 || y == 0 || z == 0) {
            plr.teleport(spawnLocation);
        } else {
            World world = getServer().getWorld(authedPlayer.getLastWorldName());
            if (world == null) {
                world = spawnLocation.getWorld();
            }

            plr.teleport(new Location(world, authedPlayer.getLastLogoutBlockX(),
                    authedPlayer.getLastLogoutBlockY(), authedPlayer.getLastLogoutBlockZ()));
        }
    }

    public void sendAPIMessage(Player plr, String action) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (DataOutputStream dos = new DataOutputStream(bos)) {
                dos.writeUTF(action);
                dos.writeUTF(plr.getUniqueId().toString());
            } catch (IOException ignore) {
                //go home Spigot, you have drunk
            }

            plr.sendPluginMessage(this, API_CHANNEL_NAME, bos.toByteArray());

        } catch (IOException ignore) {
            //oke what you're gonna do tho
        }
    }

    public void saveLocation(Player plr) {
        UUID uuid = plr.getUniqueId();
        Location location = plr.getLocation();

        PreferencesHolder.sql.safelyExecuteUpdate("UPDATE " + AuthedPlayer.AUTH_DATA_TABLE_NAME + " SET x=?,y=?,z=?,world=? WHERE uuid=?",
                location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName(), uuid.toString());
        getLogger().info("Saved location for " + uuid);
    }
}
