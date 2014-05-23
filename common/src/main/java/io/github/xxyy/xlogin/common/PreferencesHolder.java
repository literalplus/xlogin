package io.github.xxyy.xlogin.common;

import io.github.xxyy.common.sql.SafeSql;

/**
 * Holds some server-specific preferences used by the common library.
 * Those need to be set every time the library is loaded, for example from a configuration file.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
public final class PreferencesHolder {
    private static int maxUsersPerIp;
    private static int sessionExpriyTime;
    public static SafeSql sql;

    private PreferencesHolder() {
    }

    public static int getMaxUsersPerIp() {
        return PreferencesHolder.maxUsersPerIp;
    }

    public static int getSessionExpriyTime() {
        return PreferencesHolder.sessionExpriyTime;
    }

    public static void setMaxUsersPerIp(int maxUsersPerIp) {
        PreferencesHolder.maxUsersPerIp = maxUsersPerIp;
    }

    public static void setSessionExpriyTime(int sessionExpriyTime) {
        PreferencesHolder.sessionExpriyTime = sessionExpriyTime;
    }
}
