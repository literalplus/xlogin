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

package li.l1t.xlogin.bungee.misc;

import li.l1t.xlogin.bungee.XLoginPlugin;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Periodically reports gauges to StatsD, including for example player count.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-02-03
 */
public class GaugeReporter implements Runnable {
    private final XLoginPlugin plugin;

    public GaugeReporter(XLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.statsd().gauge("total_players", plugin.getProxy().getOnlineCount());
        for (ServerInfo serverInfo : plugin.getProxy().getServers().values()) {
            plugin.statsd().gauge("server_players." + serverInfo.getName(), serverInfo.getPlayers().size());
        }
    }
}
