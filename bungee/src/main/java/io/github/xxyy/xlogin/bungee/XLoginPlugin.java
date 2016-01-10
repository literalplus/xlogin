package io.github.xxyy.xlogin.bungee;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import io.github.xxyy.common.sql.SafeSql;
import io.github.xxyy.common.sql.SqlConnectable;
import io.github.xxyy.common.sql.SqlConnectables;
import io.github.xxyy.common.version.PluginVersion;
import io.github.xxyy.xlogin.bungee.authtopia.AuthtopiaHelper;
import io.github.xxyy.xlogin.bungee.authtopia.AuthtopiaListener;
import io.github.xxyy.xlogin.bungee.command.CommandChangePassword;
import io.github.xxyy.xlogin.bungee.command.CommandLogin;
import io.github.xxyy.xlogin.bungee.command.CommandPremium;
import io.github.xxyy.xlogin.bungee.command.CommandRegister;
import io.github.xxyy.xlogin.bungee.command.CommandSessions;
import io.github.xxyy.xlogin.bungee.command.CommandxLogin;
import io.github.xxyy.xlogin.bungee.command.CommandxLoginLimit;
import io.github.xxyy.xlogin.bungee.config.LocalisedMessageConfig;
import io.github.xxyy.xlogin.bungee.config.XLoginConfig;
import io.github.xxyy.xlogin.bungee.dynlist.DynlistModule;
import io.github.xxyy.xlogin.bungee.limits.IpOnlineLimitManager;
import io.github.xxyy.xlogin.bungee.limits.RateLimitManager;
import io.github.xxyy.xlogin.bungee.listener.MainListener;
import io.github.xxyy.xlogin.bungee.punishment.ban.BanModule;
import io.github.xxyy.xlogin.bungee.punishment.warn.WarnModule;
import io.github.xxyy.xlogin.common.Const;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;
import io.github.xxyy.xlogin.common.module.ModuleManager;
import io.github.xxyy.xlogin.common.module.ProxyListManager;
import io.github.xxyy.xlogin.lib.quietcord.filter.PasswordFilter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Main class interfacing with the BungeeCord plugin API.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10.5.14
 */
public class XLoginPlugin extends XLoginBungee {
    public static final String PLUGIN_VERSION = PluginVersion.ofClass(XLoginPlugin.class).toString();
    public static final String API_CHANNEL_NAME = Const.API_CHANNEL_NAME;
    public static final AuthedPlayerRepository AUTHED_PLAYER_REPOSITORY = new AuthedPlayerRepository(false);
    public static final AuthedPlayerRegistry AUTHED_PLAYER_REGISTRY = new AuthedPlayerRegistry(AUTHED_PLAYER_REPOSITORY); //TODO clear every 5m or so
    @Getter
    private XLoginConfig config;
    @Getter
    private AuthtopiaHelper authtopiaHelper;
    @Getter
    private LocalisedMessageConfig messages;
    @Getter
    private IpOnlineLimitManager onlineLimiter = new IpOnlineLimitManager(this);
    @Getter
    private RateLimitManager rateLimitManager = new RateLimitManager(this);
    @Getter
    private ProxyListManager proxyListManager;
    @Getter
    private File proxyListDir;

    @Override
    public void onEnable() {
        //Initialise configuration file
        reloadConfig();

        //Load proxy list
        proxyListManager = new ProxyListManager();
        proxyListDir = new File(getDataFolder(), "proxy-lists");
        if (proxyListDir.isDirectory() || !proxyListDir.mkdirs()){
            getLogger().warning("Couldn't create " + proxyListDir.getAbsolutePath() + "!");
        }
        File defaultProxyListFile = new File(proxyListDir, "default-proxy-list.txt");
        if (!defaultProxyListFile.isFile()){
            try {
                Files.copy(getResourceAsStream("default-proxy-list.txt"), defaultProxyListFile.toPath());
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Couldn't save default-proxy-list.txt!", e);
            }
        }
        proxyListManager.loadFromDirectory(proxyListDir, getLogger());

        //Establish database connection
        this.authtopiaHelper = new AuthtopiaHelper(this);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            PreferencesHolder.setSql(new SafeSql(getConnectableFromConfig()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //Register BungeeCord stuff
        this.getProxy().getPluginManager().registerListener(this, new AuthtopiaListener(this));
        this.getProxy().getPluginManager().registerListener(this, new MainListener(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandLogin(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandChangePassword(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandPremium(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandRegister(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandSessions(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandxLogin(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandxLoginLimit(this));
        this.getProxy().registerChannel(API_CHANNEL_NAME);
        this.getProxy().registerChannel(AuthtopiaHelper.CHANNEL_NAME);
        this.getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                onlineLimiter.recomputeOnlinePlayers();
            }
        }, 5L, 5L, TimeUnit.MINUTES);

        PreferencesHolder.setConsumer(this);

        //Enable modules
        new ModuleManager(this).enable(WarnModule.class, BanModule.class, DynlistModule.class);

        rateLimitManager.start();

        new PasswordFilter(this).inject();

        this.getLogger().info("xLogin " + PLUGIN_VERSION + " enabled!");
    }

    public void reloadConfig() {
        try {
            this.config = new XLoginConfig(this);
            this.config.init();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            getLogger().info("Could not load configuration file. Please double-check your YAML syntax with http://yaml-online-parser.appspot.com/.");
        }

        try {
            this.messages = new LocalisedMessageConfig(this);
            this.messages.init();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            getLogger().info("Could not load messages file. Please double-check your YAML syntax with http://yaml-online-parser.appspot.com/.");
        }
    }

    public SqlConnectable getConnectableFromConfig() {
        ConfigurationAdapter config = this.getProxy().getConfigurationAdapter();
        String databaseName = config.getString("mysql_database", "bungeecord");
        return SqlConnectables.fromCredentials(
                SqlConnectables.getHostString(databaseName, config.getString("mysql.hostname", "jdbc:mysql://localhost:3306/")),
                databaseName,
                config.getString("mysql.user", "bungeecord"),
                config.getString("mysql.password", "")
        );
    }

    @Override
    public void onDisable() {
        // Close database connections
        this.authtopiaHelper.destroy();
        this.authtopiaHelper = null;

        this.getLogger().info("xLogin " + PLUGIN_VERSION + " disabled!");
    }

    public void teleportToLastLocation(ProxiedPlayer plr) {
        sendAPIMessage(plr, "tp");
    }

    public void notifyRegister(ProxiedPlayer plr) {
        sendAPIMessage(plr, "register");
    }

    public void sendAPIMessage(ProxiedPlayer plr, String action) {
        sendAPIMessage(plr.getServer(), action, plr.getUniqueId().toString());
    }

    public void sendAPIMessage(Server server, String... data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (DataOutputStream dos = new DataOutputStream(bos)) {
                for (String line : data) {
                    dos.writeUTF(line);
                }
            } catch (IOException ignore) {
                //go home BungeeCord, you have drunk
            }

            server.sendData(API_CHANNEL_NAME, bos.toByteArray());

        } catch (IOException ignore) {
            //oke what you're gonna do tho
        }
    }

    public void sendAuthNotification(ProxiedPlayer plr, AuthedPlayer authedPlayer) {
        Preconditions.checkNotNull(authedPlayer.getAuthenticationProvider(), "autherPlayer.authenticationProvider");

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (DataOutputStream dos = new DataOutputStream(bos)) {
                dos.writeUTF("auth");
                dos.writeUTF(plr.getUniqueId().toString());
                dos.writeInt(authedPlayer.getAuthenticationProvider().ordinal());
            } catch (IOException ignore) {
                //go home BungeeCord, you have drunk
            }

            plr.getServer().sendData(API_CHANNEL_NAME, bos.toByteArray());

        } catch (IOException ignore) {
            //oke what you're gonna do tho
        }
    }

    @Override
    public AuthedPlayerRepository getRepository() {
        return AUTHED_PLAYER_REPOSITORY;
    }

    @Override
    public AuthedPlayerRegistry getRegistry() {
        return AUTHED_PLAYER_REGISTRY;
    }
}
