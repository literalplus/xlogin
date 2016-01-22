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

import io.github.xxyy.xlogin.common.api.punishments.BanManager;
import io.github.xxyy.xlogin.common.api.punishments.WarningManager;

/**
 * Represents a consumer of the API.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 6.6.14
 */
public interface ApiConsumer {
    /**
     * @return the AuthedPlayer repository used by this consumer.
     */
    XLoginRepository getRepository();

    /**
     * @return the AuthedPlayer registry used by this consumer.
     */
    XLoginRegistry getRegistry();

    /**
     * @return the ban manager used by this consumer, if this consumer supports bans. NULL otherwise.
     */
    BanManager getBanManager();

    /**
     * @return the warning manager used by this consumer, if this consumer supports warnings. NULL otherwise.
     */
    WarningManager getWarningManager();
}
