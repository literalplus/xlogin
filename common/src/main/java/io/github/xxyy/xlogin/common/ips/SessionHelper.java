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

    public static boolean hasValidSession(AuthedPlayer authedPlayer) {
//        Session session = EbeanManager.getEbean().find(Session.class)
//                .where().eq("user", authedPlayer.getUuid())
//                .findUnique();
        Session session;

        try(QueryResult qr = PreferencesHolder.sql.executeQueryWithResult("SELECT * FROM mt_main.xlogin_sessions WHERE user=?", authedPlayer.getUuid()).assertHasResultSet()) {
            ResultSet rs = qr.rs();
            if(rs.next()) {
                session = new Session(rs.getInt("id"), authedPlayer, IpAddressFactory.get(rs.getString("ip")), rs.getInt("expiry_time"));
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        boolean isValid = authedPlayer.isSessionsEnabled() && session.getIp().getIp().equals(authedPlayer.getLastIp()) && (System.currentTimeMillis() / 1000L) < session.getExpiryTime();

        if(!isValid) {
            PreferencesHolder.sql.safelyExecuteUpdate("DELETE FROM mt_main.xlogin_sessions WHERE id=?", session.getId());
        }

        return isValid;
    }

}
