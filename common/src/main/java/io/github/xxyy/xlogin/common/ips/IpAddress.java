package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.sql.EbeanManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents an IP address stored in database.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "ip"})
@Entity @Table(name = IpAddress.TABLE_NAME)
public class IpAddress {
    public static final String TABLE_NAME = "mt_main.xlogin_ips";
    @Id
    private int id;
    private String ip;
    @ManyToOne(targetEntity = AuthedPlayer.class, fetch = FetchType.LAZY, optional = false)
    private AuthedPlayer user;
    @Column(name = "maxusers")
    private int maxUsers;
    @Column(name = "sessions_on")
    private boolean sessionsEnabled;
    @Column(name = "last_used")
    private Timestamp lastUsed;

    public void setUser(AuthedPlayer newUser) {
        this.user = newUser;

        if(!user.getIps().contains(this)) {
            user.getIps().add(this);
        }
    }

    public static IpAddress fromIpString(String ipString) {
        return EbeanManager.getEbean().find(IpAddress.class)
                .where().eq("ip", ipString)
                .findUnique();
    }
}
