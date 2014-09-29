package io.github.xxyy.xlogin.common.api.punishments;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;

import java.sql.Timestamp;

/**
 * Represents the formal confirmation of a temporary or permanent denail of access to the network for a player.
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/09/14
 */
public interface XLoginBan extends Punishment {
    /**
     * @return the date and time when this ban expires or NULL if this ban does not expire. This may change afterwards.
     */
    @Nullable
    Timestamp getExpiryTime();

    /**
     * @return a string representation of the return value of {@link #getExpiryTime()}
     */
    @NotNull
    String getExpiryString();
}
