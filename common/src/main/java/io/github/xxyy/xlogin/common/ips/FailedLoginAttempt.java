package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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

    @NotNull
    public String toString() {
        return "io.github.xxyy.xlogin.common.ips.FailedLoginAttempt(id=" + this.id + ", ip=" + this.ip + ", user=" + this.user + ", timestamp=" + this.timestamp + ")";
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
