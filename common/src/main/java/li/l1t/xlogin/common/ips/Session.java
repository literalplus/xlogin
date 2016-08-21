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
