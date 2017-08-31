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

package li.l1t.xlogin.common.api;

import li.l1t.xlogin.common.api.punishments.BanManager;
import li.l1t.xlogin.common.api.punishments.WarningManager;

/**
 * Represents a consumer of the API.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 6.6.14
 */
public interface ApiConsumer {
    /**
     * @return the AuthedPlayer repository used by this consumer.
     */
    XLoginRepository getRepository();

    /**
     * @return the AuthedPlayer registry used by this consumer.
     */
    XLoginRegistry getRegistry();

    /**
     * @return the ban manager used by this consumer, if this consumer supports bans. NULL otherwise.
     */
    BanManager getBanManager();

    /**
     * @return the warning manager used by this consumer, if this consumer supports warnings. NULL otherwise.
     */
    WarningManager getWarningManager();
}
