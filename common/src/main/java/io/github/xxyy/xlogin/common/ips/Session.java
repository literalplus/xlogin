package io.github.xxyy.xlogin.common.ips;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Represents a session.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 22.5.14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "ip"})
@Entity @Table(name = "xlogin_sessions")
public class Session {
    private int id;

    @Column(name = "user_id")
    private AuthedPlayer user; //TODO untested

    private IpAddress ip;

    @Column(name = "expiry_time")
    private int expiryTime;
}
