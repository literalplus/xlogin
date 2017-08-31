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
