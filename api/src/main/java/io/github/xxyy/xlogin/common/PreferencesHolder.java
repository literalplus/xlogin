package io.github.xxyy.xlogin.common;

import io.github.xxyy.common.sql.SafeSql;
import io.github.xxyy.xlogin.common.api.ApiConsumer;

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
