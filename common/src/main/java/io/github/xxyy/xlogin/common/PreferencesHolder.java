package io.github.xxyy.xlogin.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Holds some server-specific preferences used by the common library.
 * Those need to be set every time the library is loaded, for example from a configuration file.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 17.5.14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreferencesHolder {
    @Getter @Setter
    private static int maxUsersPerIp;
    @Getter @Setter
    private static int sessionExpriyTime;
}
