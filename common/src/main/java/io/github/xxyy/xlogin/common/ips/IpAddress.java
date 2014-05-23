package io.github.xxyy.xlogin.common.ips;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents an IP address stored in database.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
@Entity @Table(name = IpAddress.TABLE_NAME)
public class IpAddress {
    public static final String TABLE_NAME = "xlogin_ips";
//    @Id
//    private int id;
    private String ip;
//    @Column(name = "maxusers")
    private int maxUsers;
//    @Column(name = "sessions_on")
    private boolean sessionsEnabled;
//    @Column(name = "last_used")
//    private Timestamp lastUsed;

    public IpAddress(String ip, int maxUsers, boolean sessionsEnabled) {
//        this.id = id;
        this.ip = ip;
//        this.user = user;
        this.maxUsers = maxUsers;
        this.sessionsEnabled = sessionsEnabled;
//        this.lastUsed = lastUsed;
    }

    public IpAddress() {
    }

    public static IpAddress fromIpString(String ipString) {
        return IpAddressFactory.get(ipString);
    }

//    public int getId() {
////        return this.id;
////    }

    public String getIp() {
        return this.ip;
    }

//    public AuthedPlayer getUser() {
//        return this.user;
//    }

    public int getMaxUsers() {
        return this.maxUsers;
    }

    public boolean isSessionsEnabled() {
        return this.sessionsEnabled;
    }



    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public void setSessionsEnabled(boolean sessionsEnabled) {
        this.sessionsEnabled = sessionsEnabled;
    }



    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof IpAddress)) return false;
        final IpAddress other = (IpAddress) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$ip = this.getIp();
        final Object other$ip = other.getIp();
        if (this$ip == null ? other$ip != null : !this$ip.equals(other$ip)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $ip = this.getIp();
        result = result * PRIME + ($ip == null ? 0 : $ip.hashCode());
        return result;
    }

    public boolean canEqual(Object other) {
        return other instanceof IpAddress;
    }
}
