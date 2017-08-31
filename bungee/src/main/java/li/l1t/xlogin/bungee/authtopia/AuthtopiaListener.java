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

import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.xlogin.bungee.XLoginPlugin;
import li.l1t.xlogin.bungee.limits.IpAccountLimitManager;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerFactory;
import li.l1t.xlogin.common.ips.IpAddress;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang.Validate;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * Listens for Authtopia-related events.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 10.5.14
 */
public class AuthtopiaListener implements Listener {
    private final IpAccountLimitManager accountLimiter;
    private final XLoginPlugin plugin;

    public AuthtopiaListener(final XLoginPlugin plugin) {
        this.plugin = plugin;
        accountLimiter = new IpAccountLimitManager(plugin);
    }

    @EventHandler
    public void onPreLogin(final PreLoginEvent evt) {
        plugin.statsd().increment("join-attempts");
        InetSocketAddress address = evt.getConnection().getAddress();
        if (plugin.getRateLimitManager().checkLimited(address)) {
            evt.setCancelled(true);
            evt.setCancelReason("Entschuldige, es betreten gerade zu viele Benutzer den Server. " +
                    "Bitte versuche es später erneut.");
            return;
        }

        if (plugin.getProxyListManager().isBlockedProxy(evt.getConnection().getAddress())) {
            evt.setCancelled(true);
            evt.setCancelReason("Entschuldige, aber Proxies können wir nicht erlauben.");
            return;
        }

        if (plugin.getOnlineLimiter().checkOnlineLimit(address)) {
            evt.setCancelReason(MessageFormat.format(
                    plugin.getMessages().ipAccountLimitedReached,
                    address.getAddress().toString()
            ));
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
    public void onLogin(final LoginEvent evt) {
        PendingConnection connection = evt.getConnection();
        final String ipString = connection.getAddress().getAddress().toString();
        final IpAddress ipAddress = IpAddress.fromIpString(ipString);
        accountLimiter.requestAccountLimit(connection.getUniqueId(), connection.getName(), connection.getAddress(),
                new Callback<Boolean>() {
                    @Override
                    public void done(Boolean accountLimitHit, Throwable error) {
                        if (error != null) {
                            evt.setCancelled(true);
                            evt.setCancelReason(String.format(
                                    "%sKonnte IP nicht überprüfen. Bitte versuche es später erneut. (%s)",
                                    plugin.getMessages().prefix, error.getClass().getSimpleName())
                            );
                        } else if (accountLimitHit) { //To many existing accounts on that IP
                            evt.setCancelled(true);
                            evt.setCancelReason(plugin.getMessages().prefix +
                                    MessageFormat.format(plugin.getMessages().ipAccountLimitedReached,
                                            ipString, accountLimiter.getMaxUsers(ipAddress))
                            );
                        } else { //No error and limit not hit means IP is allowed
                            plugin.getOnlineLimiter().registerOnlinePlayer(evt.getConnection().getAddress());
                        }
                        evt.completeIntent(plugin);
                    }
                });
        evt.registerIntent(plugin);
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent evt) {
        final boolean knownBefore = plugin.getRepository().isPlayerKnown(evt.getPlayer().getUniqueId());
        if (knownBefore) {
            plugin.getRepository().refreshPlayer(evt.getPlayer().getUniqueId(), evt.getPlayer().getName());
        }

        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            //TODO: This could possibly be moved into LoginEvent if we queue disconnects
            //TODO: server is actually not available in PostLogin - move plugin messages to ServerSwitch or ServerConnect
            public void run() {
                long startMs = System.currentTimeMillis();
                if (plugin.getProxy().getPlayer(evt.getPlayer().getName()) == null) {
                    plugin.getLogger().info("Player left before PostLogin: " + evt.getPlayer().getName());
                    return;
                }
                AuthedPlayer authedPlayer = null;

                if (knownBefore || evt.getPlayer().getPendingConnection().isOnlineMode()) {
                    authedPlayer = plugin.getRepository()
                            .getProfile(evt.getPlayer().getUniqueId(), evt.getPlayer().getName());
                    Validate.notNull(authedPlayer, "couldn't get profile for player in postlogin");
                    authedPlayer.setValid(true, false);
                    if (!evt.getPlayer().getName().equals(authedPlayer.getName())) {
                        authedPlayer.setName(evt.getPlayer().getName()); //Relevant for premium players
                    } //saved on auth
                }

                if (!knownBefore && evt.getPlayer().getPendingConnection().isOnlineMode()) {
                    assert authedPlayer != null;
                    authedPlayer.setPremium(true);  //TODO: Do we need this call?
                    if (plugin.getRateLimitManager().getRegisterLimit().incrementAndCheck()) {
                        evt.getPlayer().disconnect(
                                new XyComponentBuilder(
                                        "Es betreten leider gerade zu viele Spieler den Server!\n").color(ChatColor.RED)
                                        .append("Bitte versuche es in einer Minute erneut! (moj)").color(ChatColor.GOLD).create());
                        return;
                    }
                }
                boolean authed = false;

                if (evt.getPlayer().getPendingConnection().isOnlineMode() && //vvv **THIS** is where we authenticate premium players
                        plugin.getAuthtopiaHelper().registerPremium(evt.getPlayer(), authedPlayer)) {

                    evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().premiumLoggedIn));
                    authed = true;
                } else { //  vvvvv Inform player if they are premium //TODO: Is this even possible any more?
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
                        authedPlayer.setPremium(true); //TODO: Actually, autenticatePremium is only possible with premium=true

                        if (evt.getPlayer().getServer() != null) {
                            plugin.notifyRegister(evt.getPlayer());
                        }

                        AuthedPlayerFactory.save(authedPlayer); //TODO: This should theoretically be obsolete since we're now saving on auth
                    }
                } else if (!authed && plugin.getConfig().isEnableSessions()) { //**THIS** however authenticates sessions!
                    if (authedPlayer.authenticateSession(IpAddress.fromIpString(evt.getPlayer().getAddress().getAddress().toString()))) {
                        plugin.getRegistry().registerAuthentication(authedPlayer);
                        evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().sessionsLoggedIn));
                        authed = true;
                    }
                }

                if (authedPlayer != null) { //Happens if we are premium or already known
                    if (evt.getPlayer().getServer() != null) { //This notifies servers of a premium or session authentication...if any occurred
                        plugin.getAuthtopiaHelper().tryRegisterAuth(evt.getPlayer(), authedPlayer);
                    } //TODO: Is this ever called? If so, is this ever NECESSARY? We're sending that in ServerSwitch too

                    plugin.getRepository().updateProfile(authedPlayer);
                }

                if (!authed) {
                    evt.getPlayer().sendMessage(plugin.getMessages().parseMessageWithPrefix(plugin.getMessages().notLoggedIn));
                }
                plugin.statsd().recordExecutionTimeToNow("authtopia-listener.post-actual", startMs);

            }
        }, 250, TimeUnit.MILLISECONDS); //Sync disconnect causes a misleading exception message; Also: plugin messages need a server, which we don't have here
    }

    @EventHandler
    public void onServerSwitch(final ServerSwitchEvent evt) {
        plugin.getAuthtopiaHelper().publishResult(evt.getPlayer());
    }


    @EventHandler
    public void onDisconnect(final PlayerDisconnectEvent evt) {
        plugin.getOnlineLimiter().recomputeOnlinePlayers(evt.getPlayer().getAddress());
        plugin.getAuthtopiaHelper().unregisterPremium(evt.getPlayer());
    }
}
