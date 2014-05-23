package io.github.xxyy.xlogin.common.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Holds the xLogin spawn location.
 * Implementation chosen for licensing purposes.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpawnLocationHolder {
    @Getter
    private static int x;
    @Getter
    private static int y;
    @Getter
    private static int z;
    @Getter
    private static float pitch;
    @Getter
    private static float yaw;
    @Getter
    private static String worldName;

    public static void setSpawn(int newX, int newY, int newZ, float newPitch, float newYaw, String braveNewWorld) {
        x = newX;
        y = newY;
        z = newZ;
        pitch = newPitch;
        yaw = newYaw;
        worldName = braveNewWorld;
    }
}
