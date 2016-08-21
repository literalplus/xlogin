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
