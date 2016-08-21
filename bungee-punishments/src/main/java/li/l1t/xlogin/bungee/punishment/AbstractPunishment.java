/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee.punishment;

import li.l1t.common.XycConstants;
import li.l1t.xlogin.common.api.XLoginProfile;
import li.l1t.xlogin.common.api.XLoginRepository;
import li.l1t.xlogin.common.api.punishments.Punishment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @Nonnull
    private final UUID targetId;
    @Nonnull
    private final UUID sourceId;
    @Nonnull
    private final Timestamp timestamp;
    @Nullable
    private final String sourceServerName;
    @Nonnull
    private String reason;

    public AbstractPunishment(@Nonnull UUID targetId, @Nonnull UUID sourceId, @Nonnull Timestamp timestamp,
                              @Nullable String sourceServerName, @Nonnull String reason) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.timestamp = timestamp;
        this.sourceServerName = sourceServerName;
        this.reason = reason;
    }

    @Override
    @Nonnull
    public UUID getTargetId() {
        return targetId;
    }

    @Override
    @Nonnull
    public UUID getSourceId() {
        return sourceId;
    }

    @Override
    @Nonnull
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

    @Override
    @Nonnull
    public String getTargetName(XLoginRepository repo) {
        XLoginProfile profile = repo.getProfile(targetId);

        if (profile == null) {
            return "unknown/" + targetId.toString();
        }

        return profile.getName();
    }

    @Override
    @Nonnull
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    @Nonnull
    public String getReason() {
        return reason;
    }

    @Override @Nullable
    public String getSourceServerName() {
        return sourceServerName;
    }

    @Override
    @Nonnull
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
