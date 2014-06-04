package io.github.xxyy.xlogin.bungee.authtopia;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.*;
import com.google.gag.annotation.remark.ShoutOutTo;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.github.xxyy.common.lib.com.mojang.api.profiles.HttpProfileRepository;
import io.github.xxyy.common.lib.com.mojang.api.profiles.Profile;
import io.github.xxyy.common.lib.com.mojang.api.profiles.ProfileRepository;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.common.util.CommandHelper;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.Const;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Helps dealing with automagic detection of online-mode.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10.5.14
 */
@ShoutOutTo("zh32")
public class AuthtopiaHelper {

    public static final String CHANNEL_NAME = Const.AUTHTOPIA_CHANNEL_NAME;
    public static final ProfileRepository PROFILE_REPOSITORY = new HttpProfileRepository("minecraft");
    private ComboPooledDataSource dataSource = null;
    private ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
    private XLoginPlugin plugin;

    /**
     * This constructor will accept a BungeeCord instance to register with. Also
     * connects to SQL and registers the {@value #CHANNEL_NAME}
     * channel. Called in BungeeCord#start()
     *
     * @param plugin BungeeCord instance to use.
     */
    public AuthtopiaHelper(final XLoginPlugin plugin) {
        this.plugin = plugin;
//        ConfigurationAdapter config = plugin.getProxy().getConfigurationAdapter();
//        this.connect(host, port, user, pass, db, new FutureCallback<Boolean>() {
//            @Override
//            public void onSuccess(@NotNull Boolean result) {
//                if (result) {
//                    plugin.getLogger().info("[Authtopia|SQL] Successfully connected to MySQL");
//                    AuthtopiaHelper.this.createTables();
//                } else {
//                    plugin.getLogger().log(Level.SEVERE, "[Authtopia|SQL] Could not connect to SQL at {0}:{1}/{2} !", new Object[]
//                            {
//                                    host, port, db
//                            });
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Throwable thrwbl) {
//                thrwbl.printStackTrace();
//                plugin.getLogger().log(Level.SEVERE, "[Authtopia|SQL] Could not connect to SQL at {0}:{1}/{2} !", new Object[]
//                        {
//                                host, port, db
//                        });
//            }
//        });
        plugin.getProxy().registerChannel(CHANNEL_NAME);
        plugin.getLogger().log(Level.INFO, "[Authopia] Loaded BungeeCord hook.");
    }

    /**
     * Called in ServerConnector.java, when the player is being connected.
     *
     * @param plr Player to publish the results from
     */
    public void publishResult(final ProxiedPlayer plr) {
        AuthedPlayer authedPlayer = XLoginPlugin.AUTHED_PLAYER_REPOSITORY.getPlayer(plr.getUniqueId(), plr.getName());

        ByteArrayDataOutput bada = ByteStreams.newDataOutput();
        bada.writeUTF(plr.getUniqueId().toString());
        bada.writeBoolean(authedPlayer.isAuthenticated()
                && AuthedPlayer.AuthenticationProvider.MINECRAFT_PREMIUM.equals(authedPlayer.getAuthenticationProvider()));
        plr.getServer().sendData(CHANNEL_NAME, bada.toByteArray());


        tryRegisterAuth(plr, authedPlayer);
    }

    public void tryRegisterAuth(ProxiedPlayer plr, AuthedPlayer authedPlayer) {
        if (authedPlayer.isAuthenticated()) {
            plugin.sendAuthNotification(plr, authedPlayer);
            plugin.teleportToLastLocation(plr);
        }
    }

    /**
     * Fetches from database if <code>name</code> should be treated as cracked,
     * even if their account isn't. Called in
     * InitialHandler#handle(LoginRequest)
     *
     * @param name     Player to check for
     * @param callback Code to execute once the check is complete.
     */
    public void isSimulateCracked(final String name, FutureCallback<Boolean> callback) {
        try (QueryResult qr = PreferencesHolder.sql.executeQueryWithResult("SELECT COUNT(*) AS cnt FROM bungeecord.auth_list WHERE name=?", name)) {
            callback.onSuccess(!(qr.rs().next() && qr.rs().getInt("cnt") > 0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Figures out the player specific online mode for a player.
     */
    public void figureOutOnlineMode(final PreLoginEvent evt) {
        evt.registerIntent(this.plugin);

        FutureCallback<Boolean> sqlQueryCallback = new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@NotNull final Boolean queryMojang) {
                boolean onlineMode = false; //auth_list cracked override
                if (queryMojang) {
                    Profile[] profiles = PROFILE_REPOSITORY.findProfilesByNames(evt.getConnection().getName());

                    if (profiles.length == 1 && !profiles[0].getDemo()) {
                        onlineMode = true;
                    } else if (profiles.length != 0) {
                        plugin.getLogger().info(String.format("Encountered multiple profiles for username %s: %s!",
                                evt.getConnection().getName(),
                                CommandHelper.CSCollection(Arrays.asList(profiles))));
                    }
                } else {
                    onlineMode = false;
                }

                evt.getConnection().setOnlineMode(onlineMode);
                plugin.getLogger().info(evt.getConnection().getName() + ":premium=" + evt.getConnection().isOnlineMode());

                evt.completeIntent(plugin);
            }

            @Override
            public void onFailure(@NotNull Throwable thrwbl) {
                evt.setCancelReason("Datenbankfehler: " + thrwbl);
                evt.setCancelled(true);
                evt.completeIntent(AuthtopiaHelper.this.plugin);
                plugin.getLogger().log(Level.WARNING, "Database error @ handle(LoginRequest)!", thrwbl);
            }
        }; //end sqlQueryCallback

        this.isSimulateCracked(evt.getConnection().getName(), sqlQueryCallback);
    }

    /**
     * Remembers that the player identified by <code>userName</code> has a
     * Minecraft premium account and is logged in.
     *
     * @param plr Target player
     */
    public boolean registerPremium(ProxiedPlayer plr, AuthedPlayer authedPlayer) {
        if (authedPlayer.authenticatePremium()) {
            XLoginPlugin.AUTHED_PLAYER_REGISTRY.registerAuthentication(authedPlayer);

            plugin.getLogger().info("Premium player " + plr.getName() + " connected. UUID: " + plr.getUniqueId());

            return true;
        }

        return false;
    }

    public void unregisterPremium(ProxiedPlayer plr) {
//        this.premiumPlayers.remove(plr.getUniqueId());

        AuthedPlayer authedPlayer = XLoginPlugin.AUTHED_PLAYER_REPOSITORY
                .getPlayer(plr.getUniqueId(), plr.getName());

        authedPlayer.setValid(false);
        XLoginPlugin.AUTHED_PLAYER_REGISTRY.remove(plr.getUniqueId());
        XLoginPlugin.AUTHED_PLAYER_REPOSITORY.forget(plr.getUniqueId());
//        AuthedPlayerFactory.save(authedPlayer);
        plugin.getLogger().info("Player " + plr.getName() + " disconnected.");
    }

    /**
     * Connects to a SQL server.
     *
     * @param host     Hostname to use
     * @param port     Port to connect to
     * @param user     User to identify with
     * @param pass     Password to use
     * @param db       Database to query
     * @param callback Code to be executed once the connection has been
     *                 established.
     */
    private void connect(final String host, final int port, final String user, final String pass, final String db, FutureCallback<Boolean> callback) {
        ListenableFuture<Boolean> submit = service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Properties p = new Properties(System.getProperties());
                p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
                p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
                System.setProperties(p);
                dataSource = new ComboPooledDataSource();
                dataSource.setDriverClass("com.mysql.jdbc.Driver");
                dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
                dataSource.setUser(user);
                dataSource.setPassword(pass);
                dataSource.setMinPoolSize(1);
                dataSource.setMaxPoolSize(5);
                dataSource.setTestConnectionOnCheckout(true);
                dataSource.setCheckoutTimeout(3_000); // 3 seconds
                return true;

            }
        });

        Futures.addCallback(submit, callback);
    }

    /**
     * Creates necessary tables.
     */
    private void createTables() {
        Statement stmt = null;
        Connection con = null;
        try {
            con = dataSource.getConnection();
            stmt = con.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS `auth_list` ("
                    + "`id` int(100) NOT NULL AUTO_INCREMENT,"
                    + "`name` varchar(16) NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "UNIQUE KEY `name` (`name`)"
                    + ")";
            stmt.execute(sql);
        } catch (SQLException ex) {
            ProxyServer.getInstance().getLogger().log(Level.SEVERE, "[Authopia|SQL] Could not create auth_list!", ex);
        } finally {
            tryClose(stmt);
            tryClose(con);
        }
    }

    /**
     * Tries to close an {@link AutoCloseable}.
     *
     * @param closeable What to close?
     */
    private void tryClose(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ex) {
                ProxyServer.getInstance().getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Destroys this object and cleans stuff up.
     */
    public void destroy() {
        if (dataSource != null) {
            dataSource.close();
            service.shutdown();
        }
    }
}
