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
