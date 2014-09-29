package io.github.xxyy.xlogin.common.api.punishments;

import net.md_5.bungee.api.connection.Server;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;

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
     * @param sourceId the unique id of the source player or {@link io.github.xxyy.common.XycConstants#NIL_UUID} if not caused by a player
     * @param reason the reason string for the warning. Should be meaningful and not empty by convention.
     * @param sourceServer the server from which the warning originated or NULL
     * @return an object representing the created warning
     */
    @NotNull
    XLoginWarning createWarning(@NotNull UUID targetId, @NotNull UUID sourceId,
                              @Nullable Server sourceServer, @NotNull String reason);

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
    @NotNull
    List<? extends XLoginWarning> getWarningsByTarget(UUID targetId);

    /**
     * Fetches all warnings for a source.
     * @param sourceId the unique id of the source player or {@link io.github.xxyy.common.XycConstants#NIL_UUID}
     * @return a list containing requested warnings
     */
    @NotNull
    List<? extends XLoginWarning> getWarningsBySource(UUID sourceId);

    /**
     * Fetches the last warning issued by a source.
     * @param sourceId the unique id of the source player or {@link io.github.xxyy.common.XycConstants#NIL_UUID}
     * @return the latest ({@code ORDER BY timestamp DESC}) issued warning by given source or NULL if the source has not yet issued any warnings
     */
    @Nullable
    XLoginWarning getLastIssuedBy(UUID sourceId);
}
