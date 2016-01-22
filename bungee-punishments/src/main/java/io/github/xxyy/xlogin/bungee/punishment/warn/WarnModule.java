/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.punishment.warn;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.bungee.XLoginBungee;
import io.github.xxyy.xlogin.bungee.punishment.ban.BanModule;
import io.github.xxyy.xlogin.common.module.XLoginModule;
import io.github.xxyy.xlogin.common.module.annotation.CanHasPotato;
import io.github.xxyy.xlogin.common.module.annotation.Module;
import lombok.Getter;
import net.md_5.bungee.api.connection.Server;

import java.util.List;
import java.util.UUID;

/**
 * Manages the warning module.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
@Module(enableByDefault = true)
public class WarnModule extends XLoginModule implements io.github.xxyy.xlogin.common.api.punishments.WarningManager {
    @Getter
    @CanHasPotato(XLoginBungee.class)
    private XLoginBungee plugin;
    @Getter
    @CanHasPotato(BanModule.class)
    private BanModule banModule;

    @Override
    public void enable() {
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandWarn(this));
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandDeleteWarn(this));
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandListWarns(this));

        if (plugin.getWarningManager() != null) {
            plugin.getLogger().warning("Another class (" + plugin.getWarningManager().getClass().getName() +
                    ") is already managing warnings for this plugin! WarnModule won't be accessible from the API!");
            return;
        }
        plugin.setWarningManager(this); //this is probably bad practice...Someone find a better solution pls kthnx
    }

    @Override
    @NotNull
    public WarningInfo createWarning(@NotNull UUID targetId, @NotNull UUID sourceId,
                                     @Nullable Server sourceServer, @NotNull String reason) {
        return WarningInfoFactory.create(targetId, sourceId, sourceServer, reason);
    }

    @Override
    @Nullable
    public WarningInfo getWarning(int id) {
        return WarningInfoFactory.fetch(id);
    }

    @Override
    @NotNull
    public List<WarningInfo> getWarningsByTarget(UUID targetId) {
        return WarningInfoFactory.fetchByTarget(targetId);
    }

    @Override
    @NotNull
    public List<WarningInfo> getWarningsBySource(UUID sourceId) {
        return WarningInfoFactory.fetchBySource(sourceId);
    }

    @Override
    @Nullable
    public WarningInfo getLastIssuedBy(UUID sourceId) {
        return WarningInfoFactory.fetchLastIssuedBy(sourceId);
    }
}
