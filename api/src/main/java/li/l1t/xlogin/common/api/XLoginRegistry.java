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

import java.util.UUID;

/**
 * Registers authentication status of  {@link XLoginProfile}s.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public interface XLoginRegistry {
    /**
     * Checks whether the given UUID represents an authenticated player.
     *
     * @param uuid UUID to check for
     * @return whether the player represented by the given UUID is authenticated with xLogin.
     */
    boolean isAuthenticated(UUID uuid);
}
