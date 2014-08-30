package io.github.xxyy.xlogin.bungee.punishment.warn;

import io.github.xxyy.xlogin.bungee.punishment.AbstractPunishment;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a warning.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 23.8.14
 */
public final class WarningInfo extends AbstractPunishment {
    public static String WARN_TABLE_NAME = "mt_main.xlogin_warns";

    private final int id;
    private WarningState state;
    private boolean valid = true;

    protected WarningInfo(int id, UUID targetId, UUID sourceId, String reason, String sourceServerName, Timestamp timestamp, WarningState state) {
        super(targetId, sourceId, timestamp, sourceServerName, reason);
        this.id = id;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public WarningState getState() {
        return state;
    }

    public void setState(WarningState state) {
        this.state = state;
    }

    @Override
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

    public enum WarningState {
        VALID("valide"),
        INVALID("invalide"),
        UNKNOWN_REASON("unbekannter Grund"),
        DELETED("gelöscht");

        private final String desc;

        WarningState(String desc) {
            this.desc = desc;
        }

        public String getDescription() {
            return desc;
        }
    }
}
