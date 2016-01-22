/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.api;

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
