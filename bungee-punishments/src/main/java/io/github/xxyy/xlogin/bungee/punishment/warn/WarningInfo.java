/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.punishment.warn;

import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.bungee.punishment.AbstractPunishment;
import io.github.xxyy.xlogin.common.api.punishments.XLoginWarning;
import org.apache.commons.lang.Validate;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a warning.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
public final class WarningInfo extends AbstractPunishment implements XLoginWarning {
    public static String WARN_TABLE_NAME = "mt_main.xlogin_warns";

    private final int id;
    @NotNull
    private WarningState state;
    private boolean valid = true;

    protected WarningInfo(int id, @NotNull UUID targetId, @NotNull UUID sourceId, @NotNull String reason,
                          @Nullable String sourceServerName, @NotNull Timestamp timestamp, @NotNull WarningState state) {
        super(targetId, sourceId, timestamp, sourceServerName, reason);
        this.id = id;
        this.state = state;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    @NotNull
    public WarningState getState() {
        return state;
    }

    /**
     * Sets this warning's state.
     * @param state the state to set. Must not be {@link WarningInfo.WarningState#DELETED}.
     * @return this object, for convenience
     */
    public WarningInfo setState(@NotNull WarningState state) {
        Validate.notNull(state, "warning state must not be null!");
        this.state = state;
        return this;
    }

    @Override @NotNull
    public WarningInfo save() {
        WarningInfoFactory.save(this);
        return this;
    }

    @Override
    public void delete() {
        WarningInfoFactory.delete(this);
        valid = false;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarningInfo)) return false;
        if (!super.equals(o)) return false;

        WarningInfo that = (WarningInfo) o;

        if (id != that.id) return false;
        if (state != that.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (valid ? 1 : 0);
        return result;
    }

}
