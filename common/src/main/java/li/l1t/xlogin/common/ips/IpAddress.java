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
