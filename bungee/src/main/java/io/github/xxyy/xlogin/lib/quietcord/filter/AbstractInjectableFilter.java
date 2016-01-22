/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

/**
 * The following file can also be found in QuietCord, which is a F/OSS project by me.
 * You can obtain the full source at https://github.com/xxyy/quietcord .
 */

package io.github.xxyy.xlogin.lib.quietcord.filter;

import com.google.common.base.Preconditions;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a common base implementation for injectable filters. This implementation does
 * not propagate calls to previous filters when injected, however subclasses may do that.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 02/10/15
 */
public abstract class AbstractInjectableFilter implements InjectableFilter {
    private final Logger logger;
    protected Filter previousFilter = null;

    protected AbstractInjectableFilter(Logger logger) {
        Preconditions.checkNotNull(logger, "logger");
        this.logger = logger;
    }

    @Override
    public boolean isInjected() {
        return logger.getFilter() == this;
    }

    @Override
    public Filter inject() {
        if (isInjected()){
            return logger.getFilter();
        }

        previousFilter = logger.getFilter();
        logger.setFilter(this);
        return logger.getFilter();
    }

    @Override
    public boolean reset() {
        if (!isInjected()){
            logger.log(Level.WARNING,
                    "[xLo/QuietCord] Could not reset log filter {0} because replaced by {1} for logger {2}",
                    new Object[]{this, logger.getFilter(), logger.getName()});
            return false; //Maintain maximum compatibility
        } else {
            logger.setFilter(previousFilter);
            return true;
        }
    }

    @Override
    public Filter getPreviousFilter() {
        return previousFilter;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
