/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee;

import com.google.common.base.Preconditions;
import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.common.sql.SafeSql;
import io.github.xxyy.common.sql.SqlConnectable;
import io.github.xxyy.common.sql.SqlConnectables;
import io.github.xxyy.common.version.PluginVersion;
import io.github.xxyy.xlogin.bungee.authtopia.AuthtopiaHelper;
import io.github.xxyy.xlogin.bungee.authtopia.AuthtopiaListener;
import io.github.xxyy.xlogin.bungee.command.*;
import io.github.xxyy.xlogin.bungee.config.LocalisedMessageConfig;
import io.github.xxyy.xlogin.bungee.config.XLoginConfig;
import io.github.xxyy.xlogin.bungee.dynlist.DynlistModule;
import io.github.xxyy.xlogin.bungee.limits.IpOnlineLimitManager;
import io.github.xxyy.xlogin.bungee.limits.RateLimitManager;
import io.github.xxyy.xlogin.bungee.listener.MainListener;
import io.github.xxyy.xlogin.bungee.notifier.AltAccountNotifer;
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
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

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
    private File proxyListDir; //§6[§8xLogin§6] §7
    private final XyComponentBuilder prefix = new XyComponentBuilder("[").color(ChatColor.GOLD)
            .append("xLogin", ChatColor.DARK_GRAY)
            .append("] ", ChatColor.GOLD);
    @Getter
    private final AltAccountNotifer altAccountNotifer = new AltAccountNotifer(this);

    @Override
    public void onEnable() {
        //Initialise configuration file
        reloadConfig();

        //Load proxy list
        proxyListManager = new ProxyListManager();
        proxyListDir = new File(getDataFolder(), "proxy-lists");
        if (!proxyListDir.isDirectory() && !proxyListDir.mkdirs()) {
            getLogger().warning("Couldn't create " + proxyListDir.getAbsolutePath() + "!");
        }
        File defaultProxyListFile = new File(proxyListDir, "default-proxy-list.txt");
        if (!defaultProxyListFile.isFile()) {
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
        announceRegistration(plr);
    }

    private void announceRegistration(ProxiedPlayer newPlayer) {
        XyComponentBuilder welcomeBuilder = getPrefix()
                .append("Willkommen auf ✪MinoTopia✪, " + newPlayer.getName() + "!", ChatColor.GRAY);
        BaseComponent[] userComponents = new XyComponentBuilder(welcomeBuilder).create(); //create actually modifies the state ;-;
        BaseComponent[] adminComponents = welcomeBuilder.append(" [i]", ChatColor.DARK_BLUE)
                .command("/xlo user " + newPlayer.getUniqueId())
                .tooltip("§6§nZeige User-Info", "§6IP: " + newPlayer.getAddress().getAddress().getHostAddress())
                .create();

        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player.hasPermission("xlogin.admin")) {
                player.sendMessage(adminComponents);
            } else {
                player.sendMessage(userComponents);
            }
        }
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

    /**
     * Notifies the plugin that a player has authenticated in any way. This method also notifies other servers and
     * performs some routine checks on the player.
     *
     * @param plr          the player that authenticated
     * @param authedPlayer the player that authenticated
     * @see #sendAuthNotification(ProxiedPlayer, AuthedPlayer)
     */ //dat @param
    public void notifyAuthentication(ProxiedPlayer plr, AuthedPlayer authedPlayer) {
        sendAuthNotification(plr, authedPlayer);
        altAccountNotifer.scheduleCheck(authedPlayer);
    }

    /**
     * Notifies other servers that a player has authenticated in any way. Note that, normally,
     * {@link #notifyAuthentication(ProxiedPlayer, AuthedPlayer)} should be preferred over this method, except if you
     * want to explicitly bypass routine checks, such as when bulk-sending notifications.
     *
     * @param plr          the player that authenticated
     * @param authedPlayer the player that authenticated
     */
    public void sendAuthNotification(ProxiedPlayer plr, AuthedPlayer authedPlayer) {
        Preconditions.checkNotNull(authedPlayer.getAuthenticationProvider(), "authedPlayer.authenticationProvider");

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

    @Override
    public XyComponentBuilder getPrefix() {
        return new XyComponentBuilder(prefix);
    }
}
