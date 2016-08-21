/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee.punishment.ban;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import li.l1t.common.collections.Pair;
import li.l1t.xlogin.bungee.XLoginBungee;
import li.l1t.xlogin.common.api.punishments.BanManager;
import li.l1t.xlogin.common.module.XLoginModule;
import li.l1t.xlogin.common.module.annotation.CanHasPotato;
import li.l1t.xlogin.common.module.annotation.Module;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages the ban module.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
@Module(enableByDefault = true)
public class BanModule extends XLoginModule implements BanManager {
    private final LoadingCache<UUID, Pair<Boolean, BanInfo>> banInfoCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, Pair<Boolean, BanInfo>>() {
                @Override
                @Nonnull
                public Pair<Boolean, BanInfo> load(@Nonnull UUID key) throws Exception {
                    BanInfo banInfo = BanInfoFactory.fetchByTarget(BanModule.this, key);
                    return new Pair<>(banInfo != null, banInfo);
                }
            });
    @Getter
    @CanHasPotato(XLoginBungee.class)
    private XLoginBungee plugin;

    public void enable() {
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandBan(this));
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandTimeBan(this));
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandUnBan(this));
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandBanInfo(this));
        plugin.getProxy().getPluginManager().registerListener(plugin, new BanListener(this));

        if (plugin.getBanManager() != null) {
            plugin.getLogger().warning("Another class (" + plugin.getBanManager().getClass().getName() +
                    ") is already managing bans for this plugin! BanModule won't be accessible from the API!");
            return;
        }
        plugin.setBanManager(this); //this is probably bad practice...Someone find a better solution pls kthnx
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    @Nullable
    public BanInfo getBanInfo(UUID uuid) {
        Pair<Boolean, BanInfo> pair = banInfoCache.getUnchecked(uuid);
        if (pair.getLeft()) {
            BanInfo banInfo = pair.getRight();
            if (!banInfo.isValid()) {
                banInfoCache.invalidate(uuid);
                return null;
            }
            return banInfo;
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public BanInfo forceGetBanInfo(UUID uuid) {
        banInfoCache.invalidate(uuid);
        return getBanInfo(uuid);
    }

    @Override
    public boolean isBanned(UUID uuid) {
        return getBanInfo(uuid) != null;
    }


    @Nullable
    BanInfo setBannedCache(@Nonnull UUID uuid, @Nullable BanInfo banInfo) {
        if (banInfo == null) {
            banInfoCache.invalidate(uuid);
        } else {
            banInfoCache.put(banInfo.getTargetId(), new Pair<>(true, banInfo));
        }
        return banInfo;
    }

    @Override
    @Nonnull
    public BanInfo setBanned(@Nonnull UUID targetId, @Nonnull UUID sourceId, @Nullable String sourceServerName, @Nonnull String reason, @Nullable Date expiryTime) {
        plugin.statsd().increment("bans");
        //noinspection ConstantConditions
        return setBannedCache(targetId, BanInfoFactory.create(this, targetId, sourceId, sourceServerName, reason, expiryTime));
    }
}
