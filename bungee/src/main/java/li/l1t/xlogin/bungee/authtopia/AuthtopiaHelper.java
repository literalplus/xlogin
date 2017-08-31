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

package li.l1t.xlogin.bungee.authtopia;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gag.annotation.remark.ShoutOutTo;
import li.l1t.common.lib.com.mojang.api.profiles.HttpProfileRepository;
import li.l1t.common.lib.com.mojang.api.profiles.Profile;
import li.l1t.common.lib.com.mojang.api.profiles.ProfileRepository;
import li.l1t.common.sql.QueryResult;
import li.l1t.common.util.CommandHelper;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.common.Const;
import li.l1t.xlogin.common.PreferencesHolder;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
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
    //    private ComboPooledDataSource dataSource = null;
//    private ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
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
        plugin.getProxy().registerChannel(CHANNEL_NAME);
        plugin.getLogger().log(Level.INFO, "[Authopia] Loaded BungeeCord hook.");
    }

    /**
     * Called in ServerConnector.java, when the player is being connected.
     *
     * @param plr Player to publish the results from
     */
    public void publishResult(final ProxiedPlayer plr) {
        AuthedPlayer authedPlayer = plugin.getRepository().getProfile(plr.getUniqueId(), plr.getName());
        assert authedPlayer != null;

        ByteArrayDataOutput bada = ByteStreams.newDataOutput();
        bada.writeUTF(plr.getUniqueId().toString());
        bada.writeBoolean(authedPlayer.isAuthenticated()
                && AuthedPlayer.AuthenticationProvider.MINECRAFT_PREMIUM.equals(authedPlayer.getAuthenticationProvider()));
        plr.getServer().sendData(CHANNEL_NAME, bada.toByteArray());


        tryRegisterAuth(plr, authedPlayer);
    }

    public void tryRegisterAuth(ProxiedPlayer plr, AuthedPlayer authedPlayer) {
        if (authedPlayer.isAuthenticated()){
            plugin.notifyAuthentication(plr, authedPlayer);
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
    public void isSimulateCracked(final String name, final FutureCallback<Boolean> callback) {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() { //Run async because EventHandler#post(..) blocks all other events
            @Override
            public void run() {
                try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult(
                        "SELECT COUNT(*) AS cnt FROM mt_main.auth_list WHERE name=?", name)) {
                    callback.onSuccess(!(qr.rs().next() && qr.rs().getInt("cnt") > 0));
                } catch (SQLException e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    /**
     * Figures out the player specific online mode for a player.
     */
    public void figureOutOnlineMode(final PreLoginEvent evt) {
        evt.registerIntent(this.plugin);

        FutureCallback<Boolean> sqlQueryCallback = new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean queryMojang) {
                boolean onlineMode = false; //auth_list cracked override
                if (queryMojang){
                    Profile[] profiles;
                    try {
                        profiles = PROFILE_REPOSITORY.findProfilesByNames(evt.getConnection().getName());
                    } catch (IllegalStateException e) {
                        plugin.getLogger().warning(String.format("Mojang failed auth for %s: %s",
                                evt.getConnection().getName(), e.getCause().getMessage()));
                        evt.setCancelReason("§4Mojang's API-Server sind momentan down.\n" +
                                "§eDaher können wir nicht herausfinden, ob du Premium bist.\n\n" +
                                "§aMehr Info: http://xpaw.ru/mcstatus https://help.mojang.com/\n" +
                                "§6Das Wichtigste: §oKeine Panik! :)\n" +
                                "§eBitte versuche es später erneut.");
                        evt.setCancelled(true);
                        evt.completeIntent(plugin);
                        return;
                    }

                    if (profiles.length == 1 && !profiles[0].isDemo()){
                        onlineMode = true;
                    } else if (profiles.length != 0){
                        plugin.getLogger().info(String.format("Encountered multiple profiles for username %s: %s!",
                                evt.getConnection().getName(),
                                CommandHelper.CSCollection(Arrays.asList(profiles))));
                    }
                } else {
                    onlineMode = false;
                }

                evt.getConnection().setOnlineMode(onlineMode);
                plugin.getLogger().info(String.format("%s:premium=%s (%s)",
                        evt.getConnection().getName(), evt.getConnection().isOnlineMode(),
                        evt.getConnection().getAddress().getAddress().toString()));

                evt.completeIntent(plugin);
            }

            @Override
            public void onFailure(@Nonnull Throwable thrwbl) {
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
        if (authedPlayer.authenticatePremium(plr.getAddress().getAddress().toString())){
            plugin.getRegistry().registerAuthentication(authedPlayer);

//            plugin.getLogger().info("Premium player " + plr.getName() + " connected. UUID: " + plr.getUniqueId());

            return true;
        }

        return false;
    }

    public void unregisterPremium(ProxiedPlayer plr) {
        plugin.getRegistry().forget(plr.getUniqueId());
//        this.premiumPlayers.remove(plr.getUniqueId());

        AuthedPlayer authedPlayer = plugin.getRepository()
                .getProfile(plr.getUniqueId(), plr.getName());

        if (authedPlayer != null){
            authedPlayer.setValid(false, authedPlayer.isPremium() && authedPlayer.isAuthenticated());
            plugin.getRepository().forgetProfile(authedPlayer);
        }
//        plugin.getLogger().info("Player " + plr.getName() + " disconnected.");
    }

    /**
     * Destroys this object and cleans stuff up.
     */
    public void destroy() {
//        if (dataSource != null) {
//            dataSource.close();
//            service.shutdown();
//        }
    }
}
