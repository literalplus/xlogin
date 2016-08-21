/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.spigot.authedplayer;

import li.l1t.xlogin.common.authedplayer.AuthedPlayerRegistry;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerRepository;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * A Spigot-specific implementation of {@link AuthedPlayerRegistry}. Specifically doesn't check if players are
 * known to the repository when checking authentication.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30/09/15
 */ //to allow us to retrieve player data async on auth, speeding up login
public class SpigotPlayerRegistry extends AuthedPlayerRegistry {
    public SpigotPlayerRegistry(AuthedPlayerRepository repository) {
        super(repository);
    }

    @Override
    public boolean isAuthenticated(@Nonnull UUID uuid) {
        return authedPlayers.contains(uuid);
    }

    public void registerAuthentication(@Nonnull UUID uuid) {
        authedPlayers.add(uuid);
    }
}
