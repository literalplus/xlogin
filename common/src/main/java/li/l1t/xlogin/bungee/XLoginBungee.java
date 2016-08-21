/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee;

import com.timgroup.statsd.StatsDClient;
import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.xlogin.bungee.config.LocalisedMessageConfig;
import li.l1t.xlogin.bungee.config.XLoginConfig;
import li.l1t.xlogin.common.api.ApiConsumer;
import li.l1t.xlogin.common.api.punishments.BanManager;
import li.l1t.xlogin.common.api.punishments.WarningManager;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerRegistry;
import li.l1t.xlogin.common.authedplayer.AuthedPlayerRepository;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.lang.Validate;

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
        Validate.isTrue(warningManager == null, "Cannot override singleton warningManager!");
        this.warningManager = newWarningManager;
    }

    public abstract XLoginConfig getConfig();

    public abstract LocalisedMessageConfig getMessages();

    @Override
    public abstract AuthedPlayerRepository getRepository();

    @Override
    public abstract AuthedPlayerRegistry getRegistry();

    /**
     * @return a clone of the plugin's prefix, for easy construction of prefixed messages
     */
    public abstract XyComponentBuilder getPrefix();

    /**
     * @return the StatsD client used by the plugin to track metrics
     */
    public abstract StatsDClient statsd();
}
