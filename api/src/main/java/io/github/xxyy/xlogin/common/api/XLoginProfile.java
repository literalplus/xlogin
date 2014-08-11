package io.github.xxyy.xlogin.common.api;

import org.jetbrains.annotations.NotNull;

import io.github.xxyy.common.lib.com.mojang.api.profiles.Profile;

import java.util.UUID;

/**
 * Represents a xLogin player profile.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public interface XLoginProfile extends Profile {
    /**
     * @return the last recorded name of this player.
     */
    @NotNull
    String getName();

    /**
     * @return Unique ID of the player. Mojang UUID if {@link #isPremium()} returns TRUE,
     * otherwise name-based offline UUID.
     */
    @NotNull
    UUID getUniqueId();

    /**
     * @return whether this player has bought Minecraft from Mojang and enabled premium authentication.
     */
    boolean isPremium();

    /**
     * @return the last known IP of this profile.
     */
    String getLastIp();
}
