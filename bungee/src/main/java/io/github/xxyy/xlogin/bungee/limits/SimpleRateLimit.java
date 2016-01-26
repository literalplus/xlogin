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
 * Simple implementation of a rate limit. Offers reset and increment methods. Note that only the methods for setting and
 * checking limit status and getting the current value are thread-safe. All other methods are not built to be called
 * from multiple threads at the same time.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2015-12-04
 */
public class SimpleRateLimit {
    private final RateLimitManager manager;
    private final String hitMessage;
    private int threshold;
    private AtomicInteger currentValue = new AtomicInteger();

    /**
     * Creates a new rate limit.
     *
     * @param manager    the manager managing this limit
     * @param hitMessage the notice message to send through the manager on reset if the limit was reached
     * @param threshold  the threshold at which the limit should be applied
     */
    public SimpleRateLimit(RateLimitManager manager, String hitMessage, int threshold) {
        this.manager = manager;
        this.hitMessage = hitMessage;
        this.threshold = threshold;
    }


    /**
     * Resets the value to zero, sending a notice through the manager if the limit was hit since the last reset
     *
     * @return the previous value
     */
    public int reset() {
        int previousCount = currentValue.getAndSet(0);
        if (previousCount > threshold && hitMessage != null) {
            manager.sendNotice(hitMessage,
                    previousCount, threshold);
        }
        return previousCount;
    }

    /**
     * Increments the value and checks if the threshold has been reached.
     *
     * @return whether the threshold has been reached
     */
    public boolean incrementAndCheck() {
        return currentValue.incrementAndGet() > threshold;
    }

    /**
     * Just checks if the threshold has been reached.
     *
     * @return whether the threshold has been reached
     */
    public boolean isLimited() {
        return getCurrentValue() > threshold;
    }

    /**
     * Sets the threshold, i.e. the maximum value before this limit is applied.
     *
     * @param threshold the new threshold
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * @return the current threshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * @return the current value
     */
    public int getCurrentValue() {
        return currentValue.intValue();
    }

    /**
     * @return the manager managing this limit
     */
    public RateLimitManager getManager() {
        return manager;
    }
}
