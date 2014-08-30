package io.github.xxyy.xlogin.bungee.punishment.warn;

import lombok.Getter;

import io.github.xxyy.xlogin.bungee.XLoginBungee;
import io.github.xxyy.xlogin.bungee.punishment.ban.BanModule;
import io.github.xxyy.xlogin.common.module.XLoginModule;
import io.github.xxyy.xlogin.common.module.annotation.CanHasPotato;
import io.github.xxyy.xlogin.common.module.annotation.Module;

/**
 * Manages the warning module.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
@Module(enableByDefault = true)
public class WarnModule extends XLoginModule {
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
    }
}
