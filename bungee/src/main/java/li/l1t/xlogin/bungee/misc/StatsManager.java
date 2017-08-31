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

    public StatsManager(String serverHostName, int serverPort, String metricPrefix, Plugin plugin) {
        StatsDClient client1 = new NoOpStatsDClient();
        try {
            client1 = new NonBlockingStatsDClient(metricPrefix, serverHostName, serverPort, this, plugin);
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
