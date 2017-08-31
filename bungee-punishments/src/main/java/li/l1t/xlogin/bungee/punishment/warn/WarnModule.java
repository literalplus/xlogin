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

package li.l1t.xlogin.bungee.punishment.warn;

import li.l1t.xlogin.bungee.XLoginBungee;
import li.l1t.xlogin.bungee.punishment.ban.BanModule;
import li.l1t.xlogin.common.api.punishments.WarningManager;
import li.l1t.xlogin.common.module.XLoginModule;
import li.l1t.xlogin.common.module.annotation.CanHasPotato;
import li.l1t.xlogin.common.module.annotation.Module;
import lombok.Getter;
import net.md_5.bungee.api.connection.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Manages the warning module.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
@Module(enableByDefault = true)
public class WarnModule extends XLoginModule implements WarningManager {
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
    @Nonnull
    public WarningInfo createWarning(@Nonnull UUID targetId, @Nonnull UUID sourceId,
                                     @Nullable Server sourceServer, @Nonnull String reason) {
        plugin.statsd().increment("warns");
        return WarningInfoFactory.create(targetId, sourceId, sourceServer, reason);
    }

    @Override
    @Nullable
    public WarningInfo getWarning(int id) {
        return WarningInfoFactory.fetch(id);
    }

    @Override
    @Nonnull
    public List<WarningInfo> getWarningsByTarget(UUID targetId) {
        return WarningInfoFactory.fetchByTarget(targetId);
    }

    @Override
    @Nonnull
    public List<WarningInfo> getWarningsBySource(UUID sourceId) {
        return WarningInfoFactory.fetchBySource(sourceId);
    }

    @Override
    @Nullable
    public WarningInfo getLastIssuedBy(UUID sourceId) {
        return WarningInfoFactory.fetchLastIssuedBy(sourceId);
    }
}
