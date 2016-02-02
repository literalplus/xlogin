/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.authtopia;

import io.github.xxyy.common.chat.XyComponentBuilder;
import io.github.xxyy.xlogin.bungee.XLoginPlugin;
import io.github.xxyy.xlogin.bungee.limits.IpAccountLimitManager;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerFactory;
import io.github.xxyy.xlogin.common.ips.IpAddress;
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
