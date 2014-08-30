package io.github.xxyy.xlogin.common.module;

/**
 * Represents a module of xLogin which can be enabled and disabled.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */
public abstract class XLoginModule {
    private final String name;
    boolean enabled = false;

    protected XLoginModule() {
        this.name = getClass().getSimpleName();
    }

    public String getName() {
        return name;
    }

    public abstract void enable();

    public final boolean isEnabled() {
        return enabled;
    }
}
