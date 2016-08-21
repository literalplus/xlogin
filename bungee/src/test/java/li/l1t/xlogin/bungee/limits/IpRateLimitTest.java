/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
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
