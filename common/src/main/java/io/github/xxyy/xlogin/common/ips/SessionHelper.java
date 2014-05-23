package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.sql.EbeanManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helps dealing with Sessions.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionHelper {

    public static boolean hasValidSession(AuthedPlayer authedPlayer) {
        Session session = EbeanManager.getEbean().find(Session.class)
                .where().eq("user", authedPlayer.getUuid())
                .findUnique();

        boolean isValid = session != null &&
                authedPlayer.isSessionsEnabled() &&
                session.getIp().getIp().equals(authedPlayer.getLastIp()) &&
                (System.currentTimeMillis() / 1000L) < session.getExpiryTime();

        if(!isValid) {
            EbeanManager.getEbean().delete(Session.class, session);
        }

        return isValid;
    }

}
