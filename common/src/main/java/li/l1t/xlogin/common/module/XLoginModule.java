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

package li.l1t.xlogin.common.module;

import javax.annotation.Nonnull;

/**
 * Represents a module of xLogin which can be enabled and disabled.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */
public abstract class XLoginModule {
    @Nonnull
    private final String name;
    boolean enabled = false;

    protected XLoginModule() {
        this.name = getClass().getSimpleName();
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public abstract void enable();

    public final boolean isEnabled() {
        return enabled;
    }
}
