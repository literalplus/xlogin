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
