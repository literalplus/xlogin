package io.github.xxyy.xlogin.common.api;

import java.util.UUID;

/**
 * Registers authentication status of  {@link io.github.xxyy.xlogin.common.api.XLoginProfile}s.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public interface XLoginRegistry {
    /**
     * Checks whether the given UUID represents an authenticated player.
     * @param uuid UUID to check for
     * @return whether the player represented by the given UUID is authenticated with xLogin.
     */
    boolean isAuthenticated(UUID uuid);
}
