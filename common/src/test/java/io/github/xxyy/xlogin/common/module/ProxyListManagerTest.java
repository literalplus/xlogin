/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.module;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-10
 */
public class ProxyListManagerTest {

    @Test
    public void testLoadFromFile() throws Exception {
        ProxyListManager proxyListManager = new ProxyListManager();
        proxyListManager.loadFromFile(new File("../bungee/src/main/resources/default-proxy-list.txt"));
        assertThat("listed ip not loaded",
                proxyListManager.isBlockedProxy(new InetSocketAddress("100.38.22.140", 1337)), is(true));
        assertThat("unlisted ip falsely loaded",
                proxyListManager.isBlockedProxy(new InetSocketAddress("192.168.1.1", 1337)), is(false));
    }

    @Test
    public void testLoadFromDirectory() throws Exception {
        ProxyListManager proxyListManager = new ProxyListManager();
        Logger logger = Mockito.spy(Logger.getAnonymousLogger());
        //this ignores non-IP lines and therefor non-IP files:
        proxyListManager.loadFromDirectory(new File("../bungee/src/main/resources/"), logger);
        Mockito.verifyZeroInteractions(logger);
        assertThat("listed ip not loaded from dir",
                proxyListManager.isBlockedProxy(new InetSocketAddress("100.38.22.140", 1337)), is(true));
        assertThat("unlisted ip falsely loaded from dir",
                proxyListManager.isBlockedProxy(new InetSocketAddress("192.168.1.1", 1337)), is(false));
    }
}
