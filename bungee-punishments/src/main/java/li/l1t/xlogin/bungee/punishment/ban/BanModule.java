/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
