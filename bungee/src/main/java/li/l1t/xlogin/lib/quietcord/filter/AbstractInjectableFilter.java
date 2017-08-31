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

/**
 * The following file can also be found in QuietCord, which is a F/OSS project by me.
 * You can obtain the full source at https://github.com/xxyy/quietcord .
 */

package li.l1t.xlogin.lib.quietcord.filter;

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
