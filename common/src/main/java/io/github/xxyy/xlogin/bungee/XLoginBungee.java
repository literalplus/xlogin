package io.github.xxyy.xlogin.bungee;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.lang.Validate;

import io.github.xxyy.xlogin.bungee.config.LocalisedMessageConfig;
import io.github.xxyy.xlogin.bungee.config.XLoginConfig;
import io.github.xxyy.xlogin.common.api.ApiConsumer;
import io.github.xxyy.xlogin.common.api.punishments.BanManager;
import io.github.xxyy.xlogin.common.api.punishments.WarningManager;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;

/**
 * This represents the base class used to interface with other xLogin modules and the proxy on BungeeCord.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */ //sorry for the name
public abstract class XLoginBungee extends Plugin implements ApiConsumer { //Please don't kill me for this
    @Getter
    private BanManager banManager;
    @Getter
    private WarningManager warningManager;

    public void setBanManager(BanManager newBanManager) {
        Validate.isTrue(banManager == null, "Cannot override singleton banManager!");
        this.banManager = newBanManager;
    }

    public void setWarningManager(WarningManager newWarningManager) {
        Validate.isTrue(banManager == null, "Cannot override singleton warningManager!");
        this.warningManager = newWarningManager;
    }

    public abstract XLoginConfig getConfig();

    public abstract LocalisedMessageConfig getMessages();

    @Override
    public abstract AuthedPlayerRepository getRepository();

    @Override
    public abstract AuthedPlayerRegistry getRegistry();
}
