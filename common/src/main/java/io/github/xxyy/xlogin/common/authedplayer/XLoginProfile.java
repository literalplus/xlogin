package io.github.xxyy.xlogin.common.authedplayer;

import org.jetbrains.annotations.NotNull;

import java.beans.ConstructorProperties;
import java.util.UUID;

/**
 * Stores a matches xLogin profile, with name and UUID.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 8.7.14
 */
public class XLoginProfile {
    @NotNull
    private final String name;
    @NotNull
    private final UUID uniqueId;
    @NotNull
    private final boolean premium;

    @ConstructorProperties({"name", "uniqueId"})
    public XLoginProfile(@NotNull String name, @NotNull UUID uniqueId, boolean premium) {
        this.name = name;
        this.uniqueId = uniqueId;
        this.premium = premium;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof XLoginProfile)) return false;
        final XLoginProfile other = (XLoginProfile) o;
        if (!other.canEqual(this)) return false;
        if (!this.name.equals(other.name)) return false;
        if (!this.uniqueId.equals(other.uniqueId)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (name.hashCode());
        result = result * PRIME + (uniqueId.hashCode());
        return result;
    }

    public boolean canEqual(Object other) {
        return other instanceof XLoginProfile;
    }

    public String toString() {
        return "io.github.xxyy.xlogin.common.authedplayer.XLoginProfile(name=" + this.name + ", uniqueId=" + this.uniqueId + ")";
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public boolean isPremium() {
        return premium;
    }
}
