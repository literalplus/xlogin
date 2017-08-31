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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A log filter that only allows log entries that all child filters allow.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 02/10/15
 */
public class CompoundFilter extends AbstractInjectableFilter {
    private final List<Filter> filters;

    public CompoundFilter(Logger logger) {
        this(logger, new LinkedList<Filter>());
    }

    public CompoundFilter(Logger logger, List<Filter> filters) {
        super(logger);
        this.filters = new ArrayList<>(filters); //who knows what the caller might pass (e.g. Arrays.asList(...))
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        for (Filter filter : filters) {
            if (!filter.isLoggable(record)){
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a filter to this compound filter's filter list.
     *
     * @param filter the filter to add
     */
    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    /**
     * Removes a filter from this compound filter's filter list.
     *
     * @param filter the filter to remove
     */
    public void removeFilter(Filter filter) {
        filters.remove(filter);
    }

    /**
     * Checks if this compound filter's filter list contains a specific filter.
     *
     * @param filter the filter to seek
     * @return whether given filter is in the filter list
     */
    public boolean hasFilter(Filter filter) {
        return filters.contains(filter);
    }

    /**
     * @return an unmodifiable view of this compound filter's filter list.
     */
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    @Override
    public String toString() {
        return "CompoundFilter{" +
                "filters=" + filters +
                '}';
    }
}
