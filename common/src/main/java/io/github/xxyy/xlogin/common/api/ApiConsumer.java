package io.github.xxyy.xlogin.common.api;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRegistry;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayerRepository;

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
    AuthedPlayerRepository getRepository();

    /**
     * @return the AuthedPlayer registry used by this consumer.
     */
    AuthedPlayerRegistry getRegistry();
}
