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
        return currentValue.incrementAndGet() >= threshold;
    }

    /**
     * Just checks if the threshold has been reached.
     *
     * @return whether the threshold has been reached
     */
    public boolean isLimited() {
        return getCurrentValue() >= threshold;
    }

    /**
     * Returns whether this rate limit is currently experiencing suspicious behaviour that should be reported to an
     * administrator.
     *
     * @return whether this limit looks suspicious
     */
    public boolean isSuspicious() {
        return isLimited();
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
