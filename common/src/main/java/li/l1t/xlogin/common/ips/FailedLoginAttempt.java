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

import li.l1t.xlogin.common.authedplayer.AuthedPlayer;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Stores details for a failed login attempt.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
@Entity
@Table(name = FailedLoginAttempt.TABLE_NAME)
public class FailedLoginAttempt { //FIXME
    public static final String TABLE_NAME = "xlogin_login_attempts";

    @Id
    private int id;
    @ManyToOne(targetEntity = IpAddress.class, fetch = FetchType.LAZY)
    private String ip;
    @ManyToOne(targetEntity = AuthedPlayer.class, fetch = FetchType.LAZY)
    private AuthedPlayer user;
    private Timestamp timestamp;

    @java.beans.ConstructorProperties({"id", "ip", "user", "timestamp"})
    public FailedLoginAttempt(int id, String ip, AuthedPlayer user, Timestamp timestamp) {
        this.id = id;
        this.ip = ip;
        this.user = user;
        this.timestamp = timestamp;
    }

    public FailedLoginAttempt() {
    }

    public int getId() {
        return this.id;
    }

    public String getIp() {
        return this.ip;
    }

    public AuthedPlayer getUser() {
        return this.user;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Nonnull
    public String toString() {
        return "li.l1t.xlogin.common.ips.FailedLoginAttempt(id=" + this.id + ", ip=" + this.ip + ", user=" + this.user + ", timestamp=" + this.timestamp + ")";
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FailedLoginAttempt)) return false;
        final FailedLoginAttempt other = (FailedLoginAttempt) o;
        if (!other.canEqual(this)) return false;
        if (this.getId() != other.getId()) return false;
        final Object this$ip = this.getIp();
        final Object other$ip = other.getIp();
        return !(this$ip == null ? other$ip != null : !this$ip.equals(other$ip));
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        final Object $ip = this.getIp();
        result = result * PRIME + ($ip == null ? 0 : $ip.hashCode());
        return result;
    }

    public boolean canEqual(Object other) {
        return other instanceof FailedLoginAttempt;
    }
}
