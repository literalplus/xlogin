package io.github.xxyy.xlogin.bungee.authtopia;

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import io.github.xxyy.xlogin.common.ips.IpAddress;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * Listens for Authtopia-related events.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10.5.14
 */
@RequiredArgsConstructor
public class AuthtopiaListener implements Listener {
    private final XLoginPlugin plugin;

    @EventHandler
    public void onPreLogin(final PreLoginEvent evt) {
        String playerName = evt.getConnection().getName();

        if (playerName.contains("/") //Possible to screw up some permission plugins with this
                || playerName.matches("(.*)[^a-zA-Z0-9äöüÄÖÜß_\\-]+(.*)") || playerName.contains(" ")) {
            evt.setCancelled(true);
            evt.setCancelReason("Invalider Name! (Muss aus [a-zA-Z0-9äöüÄÖÜß²³_-] bestehen)");
            return;
        }

        String plrNameLC = evt.getConnection().getName().toLowerCase();

        for(String blockedName : plugin.getConfig().getBlockedNames()) {
            if(plrNameLC.contains(blockedName.toLowerCase())) {
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
        checkIp(evt);

        boolean knownBefore = XLoginPlugin.AUTHED_PLAYER_REPOSITORY.isPlayerKnown(evt.getPlayer().getUniqueId());
        AuthedPlayer authedPlayer = XLoginPlugin.AUTHED_PLAYER_REPOSITORY
                .getPlayer(evt.getPlayer().getUniqueId(), evt.getPlayer().getName());

        if(!knownBefore && evt.getPlayer().getPendingConnection().isOnlineMode()) {
            authedPlayer.setPremium(true);
        }

        boolean authed = false;

        if (evt.getPlayer().getPendingConnection().isOnlineMode() &&
                plugin.getAuthtopiaHelper().registerPremium(evt.getPlayer(), authedPlayer)) {

            evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().premiumLoggedIn));
            authed = true;
        } else { //  v^ Inform player if they are premium
            evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));

            if (evt.getPlayer().getPendingConnection().isOnlineMode() &&
                    !authedPlayer.isDisabledPremiumMessage()) {
                evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().premiumAvailable));
            }
        }


        //Make sure the user is in database
        if (!knownBefore) {
            if (authedPlayer.isAuthenticated() && authedPlayer.getAuthenticationProvider()
                    .equals(AuthedPlayer.AuthenticationProvider.MINECRAFT_PREMIUM)) {

                plugin.getProxy().broadcast(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().welcome, evt.getPlayer().getName()));
                XLoginPlugin.AUTHED_PLAYER_REPOSITORY.updateKnown(evt.getPlayer().getUniqueId(), true);
                authedPlayer.setPremium(true);
                plugin.sendAPIMessage(evt.getPlayer(), "register");
            }

            AuthedPlayerFactory.save(authedPlayer);
        } else if (!authed) {
            authedPlayer.authenticateSession();
        } else {
            plugin.sendAPIMessage(evt.getPlayer(), "register");
        }
    }

    private void checkIp(PostLoginEvent evt) {
        String ipString = evt.getPlayer().getAddress().getAddress().toString();
        IpAddress ipAddress = IpAddress.fromIpString(ipString);
        Integer maxUsers = ipAddress == null ? plugin.getConfig().getMaxUsers() : ipAddress.getMaxUsers();
        int count = 0;

        try(QueryResult qr = PreferencesHolder.sql.executeQueryWithResult("SELECT COUNT(*) AS cnt FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE user_lastip = ? AND uuid != ?", ipString, evt.getPlayer().getUniqueId())) {
            if(qr.rs().next()) {
                count = qr.rs().getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(count >= maxUsers) {
            evt.getPlayer().disconnect(plugin.getMessages().parseMessageWithPrefix(
                    plugin.getMessages().ipAccountLimitedReached, ipString, maxUsers
                    ));
            return;
        }

        Integer onlinePlayers = plugin.getIpOnlinePlayers().get(ipString);
        if(onlinePlayers != null && onlinePlayers >= maxUsers) {
            evt.getPlayer().disconnect(plugin.getMessages().parseMessageWithPrefix(
                    plugin.getMessages().ipAccountLimitedReached, ipString, maxUsers
            ));
            return;
        }

        plugin.getIpOnlinePlayers().put(ipString, onlinePlayers == null ? 1 : onlinePlayers + 1);
    }


    @EventHandler
    public void onServerSwitch(final ServerSwitchEvent evt) {
        plugin.getAuthtopiaHelper().publishResult(evt.getPlayer());
    }


    @EventHandler
    public void onDisconnect(final PlayerDisconnectEvent evt) {
        String ipString = evt.getPlayer().getAddress().getAddress().toString();
        Integer onlinePlayers = plugin.getIpOnlinePlayers().get(ipString);
        if(onlinePlayers != null) {
            if(onlinePlayers == 1) {
                plugin.getIpOnlinePlayers().remove(ipString);
            } else {
                plugin.getIpOnlinePlayers().put(ipString, onlinePlayers - 1);
            }
        }

        plugin.getAuthtopiaHelper().unregisterPremium(evt.getPlayer());

    }
}
