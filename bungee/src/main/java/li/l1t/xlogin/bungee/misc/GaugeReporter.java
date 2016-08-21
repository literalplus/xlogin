/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
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
