/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.api.punishments;

import io.github.xxyy.lib.intellij_annotations.NotNull;

/**
 * Represents a formal warning issued to a player. Once a player reaches a certain amount of warnings, additional
 * punishments will be placed automatically, such as temporary or permanent ban from the network.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/09/14
 */
public interface XLoginWarning extends Punishment {

    /**
     * @return the unique integer id of this warning.
     */
    int getId();

    /**
     * @return the state of this warning.
     */
    @NotNull
    WarningState getState();

    /**
     * Represents a state a warning can be in.
     */
    enum WarningState {
        VALID("valide"),
        INVALID("invalide"),
        UNKNOWN_REASON("unbekannter Grund"),
        DELETED("gelöscht");

        private final String desc;

        WarningState(String desc) {
            this.desc = desc;
        }

        /**
         * @return a description of this state. Currently only available in German.
         */
        public String getDescription() {
            return desc;
        }
    }
}
