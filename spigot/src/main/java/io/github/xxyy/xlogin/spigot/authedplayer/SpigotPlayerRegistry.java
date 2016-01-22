/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.spigot.authedplayer;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;

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
    public boolean isAuthenticated(@NotNull UUID uuid) {
        return authedPlayers.contains(uuid);
    }

    public void registerAuthentication(@NotNull UUID uuid) {
        authedPlayers.add(uuid);
    }
}
