/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.misc;


import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Manages interaction with the StatsD client, which sends metrics to the StatsD server over a UDP connection.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-02-03
 */
public class StatsManager implements StatsDClientErrorHandler {
    private final StatsDClient client;

    public StatsManager(String serverHostName, int serverPort, Plugin plugin) {
        StatsDClient client1 = new NoOpStatsDClient();
        try {
            client1 = new NonBlockingStatsDClient("xlogin.bungee", serverHostName, serverPort, this, plugin);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe(String.format("Could not connect to StatsD server running on %s:%d!",
                    serverHostName, serverPort));
        } finally {
            client = client1; //dirty hack, don't kill me please
        }
    }

    public StatsDClient statsd() {
        return client;
    }

    @Override
    public void handle(Exception e) {
        e.printStackTrace();
    }
}
