package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;

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

    public static boolean hasValidSession(AuthedPlayer authedPlayer, IpAddress address) {
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

    public static boolean isSessionValid(AuthedPlayer authedPlayer, Session session, IpAddress address) {
        return authedPlayer.isSessionsEnabled() &&
                session.getIp().getIp().equals(address.getIp()) &&
                (System.currentTimeMillis() / 1000L) < session.getExpiryTime() &&
                session.getUuid().equals(authedPlayer.getUuid());
    }

    public static boolean start(AuthedPlayer authedPlayer) {
        if(!authedPlayer.isAuthenticated() || !authedPlayer.isValid() || authedPlayer.isPremium()) {
            return false;
        }

        PreferencesHolder.getSql().safelyExecuteUpdate("INSERT INTO mt_main.xlogin_sessions SET user=?,ip=?,expiry_time=?",
                authedPlayer.getUuid(), authedPlayer.getLastIp(), (System.currentTimeMillis() / 1000L) + PreferencesHolder.getSessionExpiryTime());

        return true;
    }
}
