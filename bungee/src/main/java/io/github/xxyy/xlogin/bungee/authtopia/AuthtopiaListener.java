package io.github.xxyy.xlogin.bungee.authtopia;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang.Validate;

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.bungee.limits.RateLimitManager;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import io.github.xxyy.xlogin.common.ips.IpAddress;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * Listens for Authtopia-related events.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10.5.14
 */
public class AuthtopiaListener implements Listener { //FIXME DoS detection is a brute-force approach #379
    private final RateLimitManager rateLimiter;
    private final XLoginPlugin plugin;

    public AuthtopiaListener(final XLoginPlugin plugin) {
        this.plugin = plugin;
        rateLimiter = new RateLimitManager(plugin);
    }

    @EventHandler
    public void onPreLogin(final PreLoginEvent evt) {
        if (rateLimiter.checkLimited(evt.getConnection().getAddress())) {
            evt.setCancelled(true);
            evt.setCancelReason("Entschuldige, es betreten gerade zu viele Benutzer den Server. " +
                    "Bitte versuche es in 5 Minuten erneut.");
            return;
        }

        String playerName = evt.getConnection().getName();

        if (playerName.contains("/") //Possible to screw up some permission plugins with this
                || playerName.matches("(.*)[^a-zA-Z0-9äöüÄÖÜß_\\-]+(.*)") || playerName.contains(" ")) {
            evt.setCancelled(true);
            evt.setCancelReason("Invalider Name! (Muss aus [a-zA-Z0-9äöüÄÖÜß²³_-] bestehen)");
            return;
        }

        String plrNameLC = evt.getConnection().getName().toLowerCase();

        for (String blockedName : plugin.getConfig().getBlockedNames()) {
            if (plrNameLC.contains(blockedName.toLowerCase())) {
                evt.setCancelled(true);
                evt.setCancelReason(plugin.getMessages().nameBlocked);
                plugin.getLogger().info(MessageFormat.format("Player matching blocked name tried to join: '{'name={0}, blocked={1}'}'", playerName, blockedName));
                return;
            }
        }

        plugin.getAuthtopiaHelper().figureOutOnlineMode(evt);
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent evt) {
        final boolean knownBefore = plugin.getRepository().isPlayerKnown(evt.getPlayer().getUniqueId());
        if (knownBefore) {
            plugin.getRepository().refreshPlayer(evt.getPlayer().getUniqueId(), evt.getPlayer().getName());
        }

        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            public void run() {
                if (plugin.getProxy().getPlayer(evt.getPlayer().getName()) == null) {
                    plugin.getLogger().info("Player left before PostLogin: " + evt.getPlayer().getName());
                    return;
                }

                IpAddress ipAddress = checkIp(evt);

                if (ipAddress == null) {
                    return; //Can't use that IP
                }

                AuthedPlayer authedPlayer = null;

                if (knownBefore || evt.getPlayer().getPendingConnection().isOnlineMode()) {
                    authedPlayer = plugin.getRepository()
                            .getProfile(evt.getPlayer().getUniqueId(), evt.getPlayer().getName());
                    Validate.notNull(authedPlayer, "couldn't get profile for player in postlogin");
                    authedPlayer.setValid(true, false);
                }

                if (!knownBefore && evt.getPlayer().getPendingConnection().isOnlineMode()) {
                    authedPlayer.setPremium(true);
                }

                boolean authed = false;

                if (evt.getPlayer().getPendingConnection().isOnlineMode() && //vvv **THIS** is where we authenticate premium players
                        plugin.getAuthtopiaHelper().registerPremium(evt.getPlayer(), authedPlayer)) {

                    evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().premiumLoggedIn));
                    authed = true;
                } else { //  vvvvv Inform player if they are premium
                    //noinspection ConstantConditions
                    if (evt.getPlayer().getPendingConnection().isOnlineMode() &&
                            !authedPlayer.isDisabledPremiumMessage()) {
                        evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().premiumAvailable));
                    }
                }


                //Make sure the user is in database
                if (!knownBefore) { //This just notifies and registers, nothing authenticating here
                    if (authedPlayer != null && authedPlayer.isAuthenticated() &&
                            AuthedPlayer.AuthenticationProvider.MINECRAFT_PREMIUM.equals(authedPlayer.getAuthenticationProvider())) {

                        plugin.getProxy().broadcast(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().welcome, evt.getPlayer().getName()));
                        authedPlayer.setPremium(true); //TODO: Do we need this call?

                        if (evt.getPlayer().getServer() != null) {
                            plugin.notifyRegister(evt.getPlayer());
                        }

                        AuthedPlayerFactory.save(authedPlayer); //TODO: This should theoretically be obsolete since we're now saving on auth
                    }
                } else if (!authed && plugin.getConfig().isEnableSessions()) { //**THIS** however authenticates sessions!
                    if (authedPlayer.authenticateSession(ipAddress)) {
                        plugin.getRegistry().registerAuthentication(authedPlayer);
                        evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsLoggedIn));
                        authed = true;
                    }
                }

                if (authedPlayer != null) { //Happens if we are premium or already known
                    if (evt.getPlayer().getServer() != null) { //This notifies servers of a premium or session authentication...if any occurred
                        plugin.getAuthtopiaHelper().tryRegisterAuth(evt.getPlayer(), authedPlayer);
                    }

                    plugin.getRepository().updateProfile(authedPlayer);
                }

                if (!authed) {
                    evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));
                }

            }
        }, 250, TimeUnit.MILLISECONDS);
    }

    private IpAddress checkIp(PostLoginEvent evt) {
        String ipString = evt.getPlayer().getAddress().getAddress().toString();
        IpAddress ipAddress = IpAddress.fromIpString(ipString);
        Integer maxUsers = ipAddress == null ? plugin.getConfig().getMaxUsers() : ipAddress.getMaxUsers();
        int count = 0;

        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT COUNT(*) AS cnt FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE user_lastip = ? AND uuid != ? AND username != ?", ipString, evt.getPlayer().getUniqueId(), evt.getPlayer().getName())) {
            if (qr.rs().next()) {
                count = qr.rs().getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (count >= maxUsers) {
            evt.getPlayer().disconnect(plugin.getMessages().parseMessageWithPrefix(
                    plugin.getMessages().ipAccountLimitedReached, ipString, maxUsers
            ));
            return null;
        }

        Integer onlinePlayers = plugin.getIpOnlinePlayers().get(ipString);
        if (onlinePlayers != null && onlinePlayers >= maxUsers) {
            evt.getPlayer().disconnect(plugin.getMessages().parseMessageWithPrefix(
                    plugin.getMessages().ipAccountLimitedReached, ipString, maxUsers
            ));
            return null;
        }

        plugin.registerOnlineIp(ipString, onlinePlayers);

        return ipAddress;
    }


    @EventHandler
    public void onServerSwitch(final ServerSwitchEvent evt) {
//        plugin.getRepository().refreshProfile(evt.getPlayer().getUniqueId());

        plugin.getAuthtopiaHelper().publishResult(evt.getPlayer());
    }


    @EventHandler
    public void onDisconnect(final PlayerDisconnectEvent evt) {
        String ipString = evt.getPlayer().getAddress().getAddress().toString();
        Integer onlinePlayers = plugin.getIpOnlinePlayers().get(ipString);
        if (onlinePlayers != null) {
            if (onlinePlayers == 1) {
                plugin.getIpOnlinePlayers().remove(ipString);
            } else {
                plugin.getIpOnlinePlayers().put(ipString, onlinePlayers - 1);
            }
        }

        plugin.getAuthtopiaHelper().unregisterPremium(evt.getPlayer());
    }
}
