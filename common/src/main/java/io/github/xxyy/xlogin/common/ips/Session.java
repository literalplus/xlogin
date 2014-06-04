package io.github.xxyy.xlogin.common.ips;

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
    private int expiryTime;

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

    public int getExpiryTime() {
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

    public String toString() {
        return "io.github.xxyy.xlogin.common.ips.Session(id=" + this.id + ", user=" + this.uuid + ", ip=" + this.ip + ", expiryTime=" + this.expiryTime + ")";
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Session)) return false;
        final Session other = (Session) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getId() != other.getId()) return false;
        final Object this$ip = this.getIp();
        final Object other$ip = other.getIp();
        if (this$ip == null ? other$ip != null : !this$ip.equals(other$ip)) return false;
        return true;
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
