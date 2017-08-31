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
