package io.github.xxyy.xlogin.common.api;

import com.google.gag.annotation.remark.ObligatoryQuote;
import com.google.gag.enumeration.Source;

/**
 * Holds the xLogin spawn location.
 * Implementation chosen for licensing purposes.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.5.14
 */
public final class SpawnLocationHolder {
    private static int x;
    private static int y;
    private static int z;
    private static float pitch;
    private static float yaw;
    private static String worldName;

    private SpawnLocationHolder() {
    }

    @ObligatoryQuote(quote = "Brave New World", source = Source.OTHER)
    public static void setSpawn(int newX, int newY, int newZ, float newPitch, float newYaw, String braveNewWorld) {
        x = newX;
        y = newY;
        z = newZ;
        pitch = newPitch;
        yaw = newYaw;
        worldName = braveNewWorld;
    }

    public static int getX() {
        return SpawnLocationHolder.x;
    }

    public static int getY() {
        return SpawnLocationHolder.y;
    }

    public static int getZ() {
        return SpawnLocationHolder.z;
    }

    public static float getPitch() {
        return SpawnLocationHolder.pitch;
    }

    public static float getYaw() {
        return SpawnLocationHolder.yaw;
    }

    public static String getWorldName() {
        return SpawnLocationHolder.worldName;
    }
}
