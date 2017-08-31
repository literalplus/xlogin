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

package li.l1t.xlogin.bungee.limits;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class IpRateLimitTest {
    private IpRateLimit ipRateLimit;

    @Before
    public void setUp() throws Exception {
        ipRateLimit = new IpRateLimit(null, "some ip", 3);
    }

    @Test
    public void testIncrementAndCheck() throws Exception {
        assumeThat(ipRateLimit.isTimeLimited(), is(false));
        assertThat("limit wrongly kicked in after 1 hit", ipRateLimit.incrementAndCheck(), is(false));
        assertThat("limit wrongly kicked in after 2 hits", ipRateLimit.incrementAndCheck(), is(false));
        assertThat("limit didn't kick in after 3 hits", ipRateLimit.incrementAndCheck(), is(true)); //limit is three
    }

    @Test
    public void testTimeLimit() throws Exception {
        assumeThat(ipRateLimit.getThreshold(), is(3));
        assumeThat(ipRateLimit.isTimeLimited(), is(false));
        assumeThat(ipRateLimit.incrementAndCheck(), is(false));
        assumeThat(ipRateLimit.incrementAndCheck(), is(false));
        assumeThat(ipRateLimit.incrementAndCheck(), is(true)); //limit is three
        assumeThat(ipRateLimit.incrementAndCheck(), is(true));
        assumeThat(ipRateLimit.reset(), is(5));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(2)); //(0) + (5 - 3) = 2
        assumeThat(ipRateLimit.reset(), is(0));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(1)); //(2 - 1) + 0 = 1
        assumeThat(ipRateLimit.reset(), is(0));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(0)); //(1 - 1) + 0 = 0
        assumeThat(ipRateLimit.reset(), is(0));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(0));
    }

    @Test
    public void testForceLimitFor() throws Exception {
        ipRateLimit.forceLimitFor(3);
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(3));
        assumeThat(ipRateLimit.reset(), is(0));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(2));
        assumeThat(ipRateLimit.reset(), is(0));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(1));
        assumeThat(ipRateLimit.reset(), is(0));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(0));
        assumeThat(ipRateLimit.reset(), is(0));
        assertThat("time limit has wrong duration", ipRateLimit.getTimeLimit(), is(0));
    }
}
