/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.limits;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides rate limiting for actions per IP address. If an address is overly infringing, it is blocked until the
 * rate limit has been reset an adequate amount of times.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-26
 */
public class IpRateLimit extends SimpleRateLimit {
    private final String ipString;
    private AtomicInteger blockedForHits = new AtomicInteger(0);

    /**
     * Creates a new rate limit.
     *
     * @param manager   the manager managing this limit
     * @param ipString  a String uniquely identifying this IP address
     * @param threshold the threshold at which the limit should be applied
     */
    public IpRateLimit(RateLimitManager manager, String ipString, int threshold) {
        super(manager, null, threshold);
        this.ipString = ipString;
    }

    public String getIpString() {
        return ipString;
    }

    @Override
    public boolean incrementAndCheck() {
        return super.incrementAndCheck() || isTimeLimited(); //still count so that the limit doesn't get reset if the infringement is still occurring
    }

    @Override
    public boolean isLimited() {
        return isTimeLimited() || super.isLimited();
    }

    @Override
    public int reset() { //note that this is by no means thread-safe
        int previous = super.reset();
        int infringementDelta = previous - getThreshold(); //by how much the limit was infringed

        if (infringementDelta > 0) { //only time limit if the threshold was actually exceeded
            blockedForHits.addAndGet(infringementDelta); //block for N resets
        } else if (blockedForHits.get() > 0) {
            blockedForHits.decrementAndGet(); //only reduce limit if the threshold hasn't been exceeded
        }

        return previous;
    }

    /**
     * Resets this limit's time-based limit so that actions from this IP are allowed again.
     *
     * @return whether a time limit was set on the IP managed by this limit
     */
    public boolean resetTimeLimit() {
        return blockedForHits.getAndSet(-1) > 0;
    }

    /**
     * Enforces a limit on this limit for a set amount of resets. If the limit is reset periodically, this can be used
     * to block for a specific time frame.
     *
     * @param hits the amount of resets required at least until this limit is raised again
     */
    public void forceLimitFor(int hits) {
        blockedForHits.addAndGet(hits);
    }

    /**
     * Checks whether this limit is currently enforcing a time limit.
     *
     * @return whether this limit is currently enforcing a time limit
     */
    public boolean isTimeLimited() {
        return blockedForHits.get() > 0;
    }

    /**
     * Returns how many resets are required on this limit until the time limit is raised. A special value below zero
     * represents that no limit is currently enfored time-wise.
     *
     * @return the current time limit value
     */
    public int getTimeLimit() {
        return blockedForHits.get();
    }
}
