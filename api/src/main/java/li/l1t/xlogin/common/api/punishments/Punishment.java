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

package li.l1t.xlogin.common.api.punishments;

import li.l1t.xlogin.common.api.XLoginRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a generic punishment.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 27.8.14
 */
public interface Punishment {
    /**
     * @return the UUID of the target of this punishment
     */
    @Nonnull
    UUID getTargetId();

    /**
     * @return the UUID which caused this punishment or
     * {@link li.l1t.common.XycConstants#NIL_UUID} if no player can be
     * assigned to this punishment
     */
    @Nonnull
    UUID getSourceId();

    /**
     * Fetches the name of this punishment's target from a given repository.
     * @param repo the repository to use to get the requested name
     * @return the name from given repository or a string of the form "unknown/[UUID]"
     * if the given repository does not contain matching data
     */
    @Nonnull
    String getTargetName(XLoginRepository repo);

    /**
     * Fetches the name of this punishment's source from a given repository.
     * @param repo the repository to use to get the requested name
     * @return the name from given repository or a string of the form "unknown/[UUID]"
     * if the given repository does not contain matching data
     */
    @Nonnull
    String getSourceName(XLoginRepository repo);

    /**
     * @return the date and time when this punishment was issued
     */
    @Nonnull
    Timestamp getTimestamp();

    /**
     * @return the user-defined reason for this punishment.
     */
    @Nonnull
    String getReason();

    /**
     * @return the BungeeCord name of the server this punishment was issued from. The name might have changed since and this may be NULL.
     */
    @Nullable
    String getSourceServerName();

    /**
     * @return whether this punishment is still valid, that means that it still exists in database.
     */
    boolean isValid();

    /**
     * Saves this punishment.
     * @return this object, for convenience.
     */
    @Nonnull
    Punishment save();

    /**
     * Deletes this punishment from the database. This also marks this object as invalid.
     */
    void delete();

    /**
     * @return the {@link java.util.Date} corresponding to the timestamp returned by {@link #getTimestamp()}.
     */
    @Nonnull
    Date getDate();
}
