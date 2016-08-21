/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.common.ips;

import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests some critical API functions of {@link SessionHelper}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17/09/14
 */
public class SessionHelperTest {
    public static final String IP_A = "/123.456.768.1";
    public static final String IP_B = "/8.8.8.8";
    public static final UUID SOME_UUID = UUID.randomUUID();

    @Test
    public void testIsValid() {
        AuthedPlayer ap = mock(AuthedPlayer.class);
        when(ap.isSessionsEnabled()).thenReturn(false);
        Session sess = mock(Session.class);
        IpAddress ip = new IpAddress(IP_A, 4);
        IpAddress wrongIp = new IpAddress(IP_B, 69);
        when(sess.getIp()).thenReturn(ip);
        when(sess.getExpiryTime()).thenReturn(Math.floorDiv(System.currentTimeMillis(), 1000L) - 20L);
        when(sess.getUuid()).thenReturn(UUID.randomUUID().toString());

        Assert.assertThat("Session enable switch ignored!", SessionHelper.isSessionValid(ap, sess, ip), is(false));
        when(ap.isSessionsEnabled()).thenReturn(true);
        Assert.assertThat("Wrong IP ignored!", SessionHelper.isSessionValid(ap, sess, wrongIp), is(false));
        Assert.assertThat("Expiry time ignored!", SessionHelper.isSessionValid(ap, sess, ip), is(false));
        when(sess.getExpiryTime()).thenReturn(Math.floorDiv(System.currentTimeMillis(), 1000L) + 500L);
        Assert.assertThat("UUID ignored!", SessionHelper.isSessionValid(ap, sess, ip), is(false));
        when(ap.getUuid()).thenReturn(SOME_UUID.toString());
        when(sess.getUuid()).thenReturn(SOME_UUID.toString());
        Assert.assertThat("Valid session incorrectly refused!", SessionHelper.isSessionValid(ap, sess, ip), is(true));
    }
}
