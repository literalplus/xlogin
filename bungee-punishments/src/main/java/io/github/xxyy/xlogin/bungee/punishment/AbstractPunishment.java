package io.github.xxyy.xlogin.bungee.punishment;

import io.github.xxyy.common.XycConstants;
import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.common.api.punishments.Punishment;
import io.github.xxyy.xlogin.common.api.XLoginProfile;
import io.github.xxyy.xlogin.common.api.XLoginRepository;

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
    @NotNull
    private final UUID targetId;
    @NotNull
    private final UUID sourceId;
    @NotNull
    private final Timestamp timestamp;
    @Nullable
    private final String sourceServerName;
    @NotNull
    private String reason;

    public AbstractPunishment(@NotNull UUID targetId, @NotNull UUID sourceId, @NotNull Timestamp timestamp,
                              @Nullable String sourceServerName, @NotNull String reason) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.timestamp = timestamp;
        this.sourceServerName = sourceServerName;
        this.reason = reason;
    }

    @Override @NotNull
    public UUID getTargetId() {
        return targetId;
    }

    @Override @NotNull
    public UUID getSourceId() {
        return sourceId;
    }

    @Override @NotNull
    public String getSourceName(XLoginRepository repo) {
        if(XycConstants.NIL_UUID.equals(sourceId)) {
            return "CONSOLE";
        }
        XLoginProfile profile = repo.getProfile(sourceId);

        if (profile == null) {
            return "unknown/" + sourceId.toString();
        }

        return profile.getName();
    }

    @Override @NotNull
    public String getTargetName(XLoginRepository repo) {
        XLoginProfile profile = repo.getProfile(targetId);

        if (profile == null) {
            return "unknown/" + targetId.toString();
        }

        return profile.getName();
    }

    @Override @NotNull
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override @NotNull
    public String getReason() {
        return reason;
    }

    @Override @Nullable
    public String getSourceServerName() {
        return sourceServerName;
    }

    @Override@NotNull
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
