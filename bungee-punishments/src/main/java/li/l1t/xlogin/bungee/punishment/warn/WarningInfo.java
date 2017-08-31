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

package li.l1t.xlogin.bungee.punishment.warn;

import li.l1t.xlogin.bungee.punishment.AbstractPunishment;
import li.l1t.xlogin.common.api.punishments.XLoginWarning;
import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a warning.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
public final class WarningInfo extends AbstractPunishment implements XLoginWarning {
    public static String WARN_TABLE_NAME = "mt_main.xlogin_warns";

    private final int id;
    @Nonnull
    private WarningState state;
    private boolean valid = true;

    protected WarningInfo(int id, @Nonnull UUID targetId, @Nonnull UUID sourceId, @Nonnull String reason,
                          @Nullable String sourceServerName, @Nonnull Timestamp timestamp, @Nonnull WarningState state) {
        super(targetId, sourceId, timestamp, sourceServerName, reason);
        this.id = id;
        this.state = state;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    @Nonnull
    public WarningState getState() {
        return state;
    }

    /**
     * Sets this warning's state.
     * @param state the state to set. Must not be {@link WarningInfo.WarningState#DELETED}.
     * @return this object, for convenience
     */
    public WarningInfo setState(@Nonnull WarningState state) {
        Validate.notNull(state, "warning state must not be null!");
        this.state = state;
        return this;
    }

    @Override
    @Nonnull
    public WarningInfo save() {
        WarningInfoFactory.save(this);
        return this;
    }

    @Override
    public void delete() {
        WarningInfoFactory.delete(this);
        valid = false;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarningInfo)) return false;
        if (!super.equals(o)) return false;

        WarningInfo that = (WarningInfo) o;

        if (id != that.id) return false;
        if (state != that.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        result = 31 * result + state.hashCode();
        result = 31 * result + (valid ? 1 : 0);
        return result;
    }

}
