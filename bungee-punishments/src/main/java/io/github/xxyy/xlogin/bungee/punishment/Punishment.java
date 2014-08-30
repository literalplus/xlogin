package io.github.xxyy.xlogin.bungee.punishment;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a geneneric punishment.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 27.8.14
 */
public interface Punishment {
    UUID getTargetId();

    UUID getSourceId();

    String getTargetName(AuthedPlayerRepository repo);

    String getSourceName(AuthedPlayerRepository repo);

    Timestamp getTimestamp();

    String getReason();

    String getSourceServerName();

    boolean isValid();

    Punishment save();

    void delete();

    Date getDate();
}
