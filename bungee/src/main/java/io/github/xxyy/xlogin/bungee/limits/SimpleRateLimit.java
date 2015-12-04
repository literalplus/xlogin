package io.github.xxyy.xlogin.bungee.limits;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple implementation of a rate limit. Offers reset and increment methods.
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
        if (previousCount > threshold){
            manager.sendNotice(hitMessage,
                    previousCount);
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
