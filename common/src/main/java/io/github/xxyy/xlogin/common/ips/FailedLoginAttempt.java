package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Stores details for a failed login attempt.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "ip"})
@Entity
@Table(name = FailedLoginAttempt.TABLE_NAME)
public class FailedLoginAttempt {
    public static final String TABLE_NAME = "mt_main.xlogin_login_attempts";

    @Id
    private int id;
    @ManyToOne(targetEntity = IpAddress.class, fetch = FetchType.LAZY)
    private String ip;
    @ManyToOne(targetEntity = AuthedPlayer.class, fetch = FetchType.LAZY)
    private AuthedPlayer user;
    private Timestamp timestamp;

    public void setUser(AuthedPlayer newUser) {
        this.user = newUser;

        if(!user.getLoginAttempts().contains(this)) {
            user.getLoginAttempts().add(this);
        }
    }
}
