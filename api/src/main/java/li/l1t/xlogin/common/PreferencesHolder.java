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

package li.l1t.xlogin.common;

import li.l1t.common.sql.SafeSql;
import li.l1t.xlogin.common.api.ApiConsumer;

/**
 * Holds some server-specific preferences used by the common library.
 * Those need to be set every time the library is loaded, for example from a configuration file.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
public final class PreferencesHolder {
    private static int maxUsersPerIp;
    private static int sessionExpiryTime;
    private static ApiConsumer consumer;
    private static SafeSql sql;

    private PreferencesHolder() {
    }

    public static int getMaxUsersPerIp() {
        return PreferencesHolder.maxUsersPerIp;
    }

    public static void setMaxUsersPerIp(int maxUsersPerIp) {
        PreferencesHolder.maxUsersPerIp = maxUsersPerIp;
    }

    public static int getSessionExpiryTime() {
        return PreferencesHolder.sessionExpiryTime;
    }

    public static void setSessionExpiryTime(int sessionExpiryTime) {
        PreferencesHolder.sessionExpiryTime = sessionExpiryTime;
    }

    public static ApiConsumer getConsumer() {
        return consumer;
    }

    public static void setConsumer(ApiConsumer consumer) {
        PreferencesHolder.consumer = consumer;
    }

    public static SafeSql getSql() {
        return sql;
    }

    public static void setSql(SafeSql sql) {
        PreferencesHolder.sql = sql;
    }
}
