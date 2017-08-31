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

import javax.annotation.Nonnull;

/**
 * Represents a session.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
//@Entity @Table(name = "xlogin_sessions")
public class Session {
    private int id;

//    @Column(name = "user_id")
    private String uuid; //TODO untested

    private IpAddress ip;

//    @Column(name = "expiry_time")
    private long expiryTime;

//    @java.beans.ConstructorProperties({"id", "user", "ip", "expiryTime"})
    public Session(int id, String uuid, IpAddress ip, int expiryTime) {
        this.id = id;
        this.uuid = uuid;
        this.ip = ip;
        this.expiryTime = expiryTime;
    }

    public Session() {
    }

    public int getId() {
        return this.id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public IpAddress getIp() {
        return this.ip;
    }

    public long getExpiryTime() {
        return this.expiryTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(String uuid) {
        this.uuid = uuid;
    }

    public void setIp(IpAddress ip) {
        this.ip = ip;
    }

    public void setExpiryTime(int expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Nonnull
    public String toString() {
        return "li.l1t.xlogin.common.ips.Session(id=" + this.id + ", user=" + this.uuid + ", ip=" + this.ip + ", expiryTime=" + this.expiryTime + ")";
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Session)) return false;
        final Session other = (Session) o;
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
        return other instanceof Session;
    }
}
