/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.common.ips;

import li.l1t.common.sql.QueryResult;
import li.l1t.xlogin.common.PreferencesHolder;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Helps dealing with Sessions.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
public final class SessionHelper {
    private SessionHelper() {
    }

    public static boolean hasValidSession(@Nonnull AuthedPlayer authedPlayer, @Nonnull IpAddress address) {
        Session session;

        try(QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT * FROM mt_main.xlogin_sessions WHERE user=?", authedPlayer.getUuid()).assertHasResultSet()) {
            ResultSet rs = qr.rs();
            if(rs.next()) {
                session = new Session(rs.getInt("id"), rs.getString("user"), IpAddressFactory.get(rs.getString("ip")), rs.getInt("expiry_time"));
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        boolean valid = isSessionValid(authedPlayer, session, address);

        if(!valid) {
            PreferencesHolder.getSql().safelyExecuteUpdate("DELETE FROM mt_main.xlogin_sessions WHERE id=? OR user=?", session.getId(), session.getUuid());
        }

        return valid;
    }

    public static boolean isSessionValid(@Nonnull AuthedPlayer authedPlayer, @Nonnull Session session, @Nonnull IpAddress address) {
        return authedPlayer.isSessionsEnabled() &&
                session.getIp().getIp().equals(address.getIp()) &&
                (System.currentTimeMillis() / 1000L) < session.getExpiryTime() &&
                session.getUuid().equals(authedPlayer.getUuid());
    }

    public static boolean start(@Nonnull AuthedPlayer authedPlayer) {
        if(!authedPlayer.isAuthenticated() || !authedPlayer.isValid() || authedPlayer.isPremium()) {
            return false;
        }

        PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO mt_main.xlogin_sessions SET user=?,ip=?,expiry_time=?",
                authedPlayer.getUuid(), authedPlayer.getLastIp(), (System.currentTimeMillis() / 1000L) + PreferencesHolder.getSessionExpiryTime());

        return true;
    }
}
