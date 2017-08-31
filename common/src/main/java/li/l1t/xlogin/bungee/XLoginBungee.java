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
