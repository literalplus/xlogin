package io.github.xxyy.xlogin.common.api;

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
}