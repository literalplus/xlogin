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

package li.l1t.xlogin.lib.quietcord.filter;

import java.util.logging.Filter;
import java.util.logging.Logger;

/**
 * A logger-specific log filter that can be injected into and removed from its logger.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 02/10/15
 */
public interface InjectableFilter extends Filter {
    /**
     * @return the logger associated with this injectable filter
     */
    Logger getLogger();

    /**
     * Gets the filter that was replaced or extended by this filter. A return value
     * of null may mean that either there was no previous filter or the filter is
     * not currently injected.
     *
     * @return the previous filter or null
     */
    Filter getPreviousFilter();

    /**
     * Checks whether this filter is currently injected into the logger. The filter is
     * not injected if the logger's filter has ben changed somewhere else.
     *
     * @return whether the filter is currently injected into its logger
     */
    boolean isInjected();

    /**
     * Attempts to inject this filter into its logger. Implementations may choose to
     * use a wrapper filter for compatibility with filters already added to the logger.
     *
     * @return the filter that was injected, need not be this filter
     */
    Filter inject();

    /**
     * Attempts to reset the logger's filter to its state before this filter was injected.
     * The behaviour of this method is undefined if that logger's current filter was not
     * injected by this instance's {@link #inject() inject method}, and entirely
     * implementation-dependent.
     *
     * @return whether the logger was reset to its previous state
     */
    boolean reset();
}
