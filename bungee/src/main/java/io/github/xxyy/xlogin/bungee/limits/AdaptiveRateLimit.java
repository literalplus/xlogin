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

import com.google.common.base.Preconditions;

/**
 * An adaptive rate limit, which adapts to load by reducing the threshold by a given factor.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2015-12-05
 */
public class AdaptiveRateLimit extends SimpleRateLimit {
    private int baseThreshold;
    private int minThreshold;
    private float loadFactor;

    /**
     * Creates a new rate limit.
     *
     * @param manager       the manager managing this limit
     * @param hitMessage    the notice message to send through the manager on reset if the limit was reached
     * @param baseThreshold the base threshold at which the limit should be applied
     * @param loadFactor    the factor by which the threshold is reduced when hit
     * @param minThreshold  the minimum dynamic threshold
     */
    public AdaptiveRateLimit(RateLimitManager manager, String hitMessage, int baseThreshold, float loadFactor, int minThreshold) {
        super(manager, hitMessage, baseThreshold);
        Preconditions.checkArgument(loadFactor > 0 && loadFactor < 1, "Invalid loadFactor! expected: 0 < loadFactor < 1, got: %s", loadFactor);
        this.baseThreshold = baseThreshold;
        this.loadFactor = loadFactor;
        this.minThreshold = minThreshold;
    }

    @Override
    public int reset() {
        int previousCount = super.reset();
        if (previousCount > getThreshold()){ //If the threshold has been reached, reduce it
            applyLoadFactor();
        } else if (getThreshold() < getBaseThreshold()){ //Otherwise, if it is reduced, gradually revert it
            revertLoadFactor();
        }
        return previousCount;
    }

    /**
     * @return the base threshold for this adaptive limit
     */
    public int getBaseThreshold() {
        return baseThreshold;
    }

    /**
     * Sets the base threshold for this adaptive limit. During normal operation, this will be the threshold. However, in
     * case of limit, the threshold will be dynamically reduced.
     *
     * @param baseThreshold the new base threshold
     */
    public void setBaseThreshold(int baseThreshold) {
        this.baseThreshold = baseThreshold;
        setThreshold(baseThreshold);
    }

    /**
     * Resets the threshold of this limit to the base threshold.
     */
    public void resetThreshold() {
        setThreshold(baseThreshold);
    }

    private void applyLoadFactor() {
        int newThreshold = (int) Math.ceil(getBaseThreshold() * loadFactor);
        if (newThreshold < minThreshold){
            newThreshold = minThreshold;
        }
        setThreshold(newThreshold);
    }

    private void revertLoadFactor() {
        if (getThreshold() >= getBaseThreshold()){
            return;
        }

        /*
        maths magic! G...previous (higher) load factor, P...current load factor
        apply operation: G*0.75=P -> inverse: P/0.75 = G
         */
        int newThreshold = (int) Math.ceil(getBaseThreshold() / loadFactor);
        if (newThreshold > baseThreshold){
            newThreshold = baseThreshold;
        }
        setThreshold(newThreshold);
    }
}
