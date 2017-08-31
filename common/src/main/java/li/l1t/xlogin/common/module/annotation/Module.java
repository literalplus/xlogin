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

package li.l1t.xlogin.common.module.annotation;

import li.l1t.xlogin.common.module.XLoginModule;

import javax.annotation.Nonnull;
import java.lang.annotation.*;

/**
 * Annotation used to annotate various parameters of modules
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {
    @Nonnull Class<? extends XLoginModule>[] dependencies() default {};

    boolean enableByDefault() default false;
}
