package io.github.xxyy.xlogin.common.ips;

import org.junit.Assert;
import org.junit.Test;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests some critical API functions of {@link io.github.xxyy.xlogin.common.ips.SessionHelper}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17/09/14
 */
public class SessionHelperTest {
    public static final String IP_A = "/123.456.768.1";
    public static final String IP_B = "/8.8.8.8";

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
        when(sess.getUuid()).thenReturn(ap.getUuid());
        Assert.assertThat("Valid session incorrectly refused!", SessionHelper.isSessionValid(ap, sess, ip), is(true));
    }
}
