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

public class SimpleRateLimitTest {
    private SimpleRateLimit simpleRateLimit;

    @Before
    public void setUp() throws Exception {
        simpleRateLimit = new SimpleRateLimit(null, null, 3);
    }

    @Test
    public void testIncrementAndCheck() throws Exception {
        assumeThat(simpleRateLimit.isLimited(), is(false));
        assertThat("limit wrongly kicked in after 1 hit", simpleRateLimit.incrementAndCheck(), is(false));
        assertThat("limit wrongly kicked in after 2 hits", simpleRateLimit.incrementAndCheck(), is(false));
        assertThat("limit didn't kick in after 3 hits", simpleRateLimit.incrementAndCheck(), is(true)); //limit is three
        assertThat("isLimited doesn't reflect limit state", simpleRateLimit.isLimited(), is(true)); //limit is three
        assertThat("limit didn't kick in after 4 hits", simpleRateLimit.incrementAndCheck(), is(true));
    }

    @Test
    public void testReset() throws Exception {
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(true)); //limit is three
        assertThat("reset method didn't return correct value", simpleRateLimit.reset(), is(3));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(true)); //limit is three
    }

    @Test
    public void testSetThreshold() throws Exception {
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(true)); //limit is three
        assertThat("reset method didn't return correct value", simpleRateLimit.reset(), is(3));
        simpleRateLimit.setThreshold(5);
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assumeThat(simpleRateLimit.incrementAndCheck(), is(false));
        assertThat("limit didn't kick in after 5 hits", simpleRateLimit.incrementAndCheck(), is(true)); //limit is five now
    }
}
