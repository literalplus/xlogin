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

import net.md_5.bungee.api.connection.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Represents an interface to a warnings system, which allows to issue formal warnings to players. Upon reaching a certain
 * amount of warnings, players will automatically receive accurate punishments.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30/09/14
 */
public interface WarningManager {
    /**
     * Creates a new warning and saves it to database.
     * @param targetId the unique id of the target player
     * @param sourceId the unique id of the source player or {@link li.l1t.common.XycConstants#NIL_UUID} if not caused by a player
     * @param reason the reason string for the warning. Should be meaningful and not empty by convention.
     * @param sourceServer the server from which the warning originated or NULL
     * @return an object representing the created warning
     */
    @Nonnull
    XLoginWarning createWarning(@Nonnull UUID targetId, @Nonnull UUID sourceId,
                                @Nullable Server sourceServer, @Nonnull String reason);

    /**
     * Fetches a warning from database by its unique integer id.
     * @param id the id to look for
     * @return the requested warning or NULL if there is no such warning
     */
    @Nullable
    XLoginWarning getWarning(int id);

    /**
     * Fetches all warnings for a target.
     * @param targetId the unique id of the target player
     * @return a list containing requested warnings
     */
    @Nonnull
    List<? extends XLoginWarning> getWarningsByTarget(UUID targetId);

    /**
     * Fetches all warnings for a source.
     * @param sourceId the unique id of the source player or {@link li.l1t.common.XycConstants#NIL_UUID}
     * @return a list containing requested warnings
     */
    @Nonnull
    List<? extends XLoginWarning> getWarningsBySource(UUID sourceId);

    /**
     * Fetches the last warning issued by a source.
     * @param sourceId the unique id of the source player or {@link li.l1t.common.XycConstants#NIL_UUID}
     * @return the latest ({@code ORDER BY timestamp DESC}) issued warning by given source or NULL if the source has not yet issued any warnings
     */
    @Nullable
    XLoginWarning getLastIssuedBy(UUID sourceId);
}
