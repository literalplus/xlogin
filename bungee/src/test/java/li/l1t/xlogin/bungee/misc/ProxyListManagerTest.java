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
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProxyListManagerTest {

    @Test
    public void testLoadFromFile() throws Exception {
        ProxyListManager proxyListManager = new ProxyListManager(new NoOpStatsDClient());
        proxyListManager.loadFromFile(new File("../bungee/src/main/resources/default-proxy-list.txt"));
        assertThat("listed ip not loaded (range)",
                proxyListManager.isBlockedProxy(new InetSocketAddress("101.255.17.245", 1337)), is(true));
        assertThat("listed ip not loaded (range)",
                proxyListManager.isBlockedProxy(new InetSocketAddress("101.255.17.246", 1337)), is(true));
        assertThat("listed ip not loaded (range broadcast)",
                proxyListManager.isBlockedProxy(new InetSocketAddress("101.255.17.247", 1337)), is(true));
        assertThat("listed ip not loaded (single)",
                proxyListManager.isBlockedProxy(new InetSocketAddress("103.16.114.1", 1337)), is(true));
        assertThat("unlisted ip falsely loaded",
                proxyListManager.isBlockedProxy(new InetSocketAddress("192.168.1.1", 1337)), is(false));
    }

    @Test
    public void testLoadFromDirectory() throws Exception {
        ProxyListManager proxyListManager = new ProxyListManager(new NoOpStatsDClient());
        Logger logger = Mockito.spy(Logger.getAnonymousLogger());
        //this ignores non-IP lines and therefore non-IP files:
        proxyListManager.loadFromDirectory(new File("../bungee/src/main/resources/"), logger);
        Mockito.verifyZeroInteractions(logger);
        assertThat("listed ip not loaded from dir",
                proxyListManager.isBlockedProxy(new InetSocketAddress("100.38.22.140", 1337)), is(true));
        assertThat("unlisted ip falsely loaded from dir",
                proxyListManager.isBlockedProxy(new InetSocketAddress("192.168.1.1", 1337)), is(false));
    }
}
