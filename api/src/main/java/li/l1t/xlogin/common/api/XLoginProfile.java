/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.common.api;


import li.l1t.common.lib.com.mojang.api.profiles.Profile;

import javax.annotation.Nonnull;
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
    @Nonnull
    String getName();

    /**
     * @return Unique ID of the player. Mojang UUID if {@link #isPremium()} returns TRUE,
     * otherwise name-based offline UUID.
     */
    @Nonnull
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
