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
 * Represents an IP address stored in database.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
public class IpAddress {
    public static final String TABLE_NAME = "mt_main.xlogin_ips";
    private String ip;
    private int maxUsers;

    protected IpAddress(String ip, int maxUsers) {
        this.ip = ip;
        this.maxUsers = maxUsers;
    }

    public static IpAddress fromIpString(String ipString) {
        return IpAddressFactory.get(ipString);
    }

    public String getIp() {
        return this.ip;
    }

    public int getMaxUsers() {
        return this.maxUsers;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    /**
     * Makes this object adapt the properties of the argument.
     * @param other Object to source properties from.
     */
    public void adaptToProperties(@Nonnull IpAddress other) {
        this.maxUsers = other.getMaxUsers();
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof IpAddress)) return false;
        final IpAddress other = (IpAddress) o;
        if (!other.canEqual(this)) return false;
        final Object this$ip = this.getIp();
        final Object other$ip = other.getIp();
        return !(this$ip == null ? other$ip != null : !this$ip.equals(other$ip));
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
