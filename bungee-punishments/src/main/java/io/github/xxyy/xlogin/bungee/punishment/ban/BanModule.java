package io.github.xxyy.xlogin.bungee.punishment.ban;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;

import io.github.xxyy.common.collections.Pair;
import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.bungee.XLoginBungee;
import io.github.xxyy.xlogin.common.module.XLoginModule;
import io.github.xxyy.xlogin.common.module.annotation.CanHasPotato;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages the ban module.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
public class BanModule extends XLoginModule {
    private final LoadingCache<UUID, Pair<Boolean, BanInfo>> banInfoCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, Pair<Boolean, BanInfo>>() {
                @Override
                @NotNull
                public Pair<Boolean, BanInfo> load(@NotNull UUID key) throws Exception {
                    BanInfo banInfo = BanInfoFactory.fetchByTarget(key);
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
    }

    @SuppressWarnings("ConstantConditions")
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

    public BanInfo forceGetBanInfo(UUID uuid) {
        banInfoCache.invalidate(uuid);
        return getBanInfo(uuid);
    }

    public boolean isBanned(UUID uuid) {
        return getBanInfo(uuid) != null;
    }

    void setBanned(@NotNull UUID uuid, @Nullable BanInfo banInfo) {
        if (banInfo == null) {
            banInfoCache.invalidate(uuid);
        } else {
            banInfoCache.put(banInfo.getTargetId(), new Pair<>(true, banInfo));
        }
    }

    public void setBanned(@NotNull UUID targetId, @NotNull UUID sourceId, @NotNull String sourceServerName, @NotNull String reason, @Nullable Date expiryTime) {
        setBanned(targetId, BanInfoFactory.create(targetId, sourceId, sourceServerName, reason, expiryTime));
    }
}
