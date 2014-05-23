package io.github.xxyy.xlogin.bungee;

import io.github.xxyy.common.sql.SqlConnectable;
import io.github.xxyy.common.sql.SqlConnectables;
import io.github.xxyy.common.version.PluginVersion;
import io.github.xxyy.xlogin.bungee.authtopia.AuthtopiaHelper;
import io.github.xxyy.xlogin.bungee.authtopia.AuthtopiaListener;
import io.github.xxyy.xlogin.bungee.command.CommandLogin;
import io.github.xxyy.xlogin.bungee.config.LocalisedMessageConfig;
import io.github.xxyy.xlogin.bungee.config.XLoginConfig;
import io.github.xxyy.xlogin.bungee.listener.MainListener;
import io.github.xxyy.xlogin.common.Const;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;
import io.github.xxyy.xlogin.common.sql.EbeanManager;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class interfacing with the BungeeCord plugin API.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10.5.14
 */
public class XLoginPlugin extends Plugin {
    public static final String PLUGIN_VERSION = PluginVersion.ofClass(XLoginPlugin.class).toString();
    public static final String API_CHANNEL_NAME = Const.API_CHANNEL_NAME;
    @Getter
    private XLoginConfig config;
    @Getter
    private AuthtopiaHelper authtopiaHelper;
    public static final AuthedPlayerRegistry AUTHED_PLAYER_REGISTRY = new AuthedPlayerRegistry(); //TODO clear every 5m or so
    public static final AuthedPlayerRepository AUTHED_PLAYER_REPOSITORY = new AuthedPlayerRepository();
    @Getter
    private final Map<String, Integer> ipOnlinePlayers = new HashMap<>();
    @Getter
    private LocalisedMessageConfig messages;

    @Override
    public void onEnable() {
        //Initialise configuration file
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

        //Establish database connection
        this.authtopiaHelper = new AuthtopiaHelper(this);
        EbeanManager.initialise(getConnectableFromConfig());

        //Register BungeeCord stuff
        this.getProxy().getPluginManager().registerListener(this, new AuthtopiaListener(this));
        this.getProxy().getPluginManager().registerListener(this, new MainListener(this));
        this.getProxy().getPluginManager().registerCommand(this, new CommandLogin(this));
        this.getProxy().registerChannel(API_CHANNEL_NAME);
        this.getProxy().registerChannel(AuthtopiaHelper.CHANNEL_NAME);

        //Register auth callback
        registerAuthCallback();

        this.getLogger().info("xLogin " + PLUGIN_VERSION + " enabled!");
    }

    public SqlConnectable getConnectableFromConfig() {
        ConfigurationAdapter config = this.getProxy().getConfigurationAdapter();
        String databaseName = config.getString("mysql.database", "bungee");
        return SqlConnectables.fromCredentials(
                SqlConnectables.getHostString(config.getString("mysql.host", "localhost"), config.getInt("mysql.port", 3306), databaseName),
                config.getString("mysql.user", "root"),
                config.getString("mysql.passwd", ""),
                databaseName
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
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (DataOutputStream dos = new DataOutputStream(bos)) {
                dos.writeUTF(action);
                dos.writeUTF(plr.getUniqueId().toString());
            } catch (IOException ignore) {
                //go home BungeeCord, you have drunk
            }

            plr.getServer().sendData(API_CHANNEL_NAME, bos.toByteArray());

        } catch (IOException ignore) {
            //oke what you're gonna do tho
        }
    }

    private void registerAuthCallback() {
        AUTHED_PLAYER_REGISTRY.setAuthenticationCallback(new Callback<AuthedPlayer>() {
            @Override
            public void done(AuthedPlayer authedPlayer, Throwable throwable) {
                ProxiedPlayer plr = XLoginPlugin.this.getProxy().getPlayer(authedPlayer.getName()); //TODO lookup by UUID

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
        });
    }
}
