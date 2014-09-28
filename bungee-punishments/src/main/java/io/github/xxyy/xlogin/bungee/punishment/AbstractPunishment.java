package io.github.xxyy.xlogin.bungee.punishment;

import io.github.xxyy.common.XycConstants;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Abstract base methods for Punishments.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 27.8.14
 */
public abstract class AbstractPunishment implements Punishment {
    private final UUID targetId;
    private final UUID sourceId;
    private final Timestamp timestamp;
    private final String sourceServerName;
    private String reason;

    public AbstractPunishment(UUID targetId, UUID sourceId, Timestamp timestamp, String sourceServerName, String reason) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.timestamp = timestamp;
        this.sourceServerName = sourceServerName;
        this.reason = reason;
    }

    @Override
    public UUID getTargetId() {
        return targetId;
    }

    @Override
    public UUID getSourceId() {
        return sourceId;
    }

    @Override
    public String getSourceName(AuthedPlayerRepository repo) {
        if(XycConstants.NIL_UUID.equals(sourceId)) {
            return "CONSOLE";
        }

        AuthedPlayer authedPlayer = repo.getProfile(sourceId);

        if (authedPlayer == null) {
            return "unknown/" + sourceId.toString();
        }

        return authedPlayer.getName();
    }

    @Override
    public String getTargetName(AuthedPlayerRepository repo) {
        AuthedPlayer authedPlayer = repo.getProfile(targetId);

        if (authedPlayer == null) {
            return "unknown/" + targetId.toString();
        }

        return authedPlayer.getName();
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public String getSourceServerName() {
        return sourceServerName;
    }

    @Override
    public Date getDate() {
        return new Date(getTimestamp().getTime());
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPunishment)) return false;

        AbstractPunishment that = (AbstractPunishment) o;

        if (!sourceId.equals(that.sourceId)) return false;
        if (!targetId.equals(that.targetId)) return false;
        if (!timestamp.equals(that.timestamp)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = targetId.hashCode();
        result = 31 * result + sourceId.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }
}
