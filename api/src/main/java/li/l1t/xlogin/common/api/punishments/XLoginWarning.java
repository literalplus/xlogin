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

package li.l1t.xlogin.common.api.punishments;

import javax.annotation.Nonnull;

/**
 * Represents a formal warning issued to a player. Once a player reaches a certain amount of warnings, additional
 * punishments will be placed automatically, such as temporary or permanent ban from the network.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/09/14
 */
public interface XLoginWarning extends Punishment {

    /**
     * @return the unique integer id of this warning.
     */
    int getId();

    /**
     * @return the state of this warning.
     */
    @Nonnull
    WarningState getState();

    /**
     * Represents a state a warning can be in.
     */
    enum WarningState {
        VALID("valide"),
        INVALID("invalide"),
        UNKNOWN_REASON("unbekannter Grund"),
        DELETED("gelöscht");

        private final String desc;

        WarningState(String desc) {
            this.desc = desc;
        }

        /**
         * @return a description of this state. Currently only available in German.
         */
        public String getDescription() {
            return desc;
        }
    }
}
