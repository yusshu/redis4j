package team.unnamed.redis;

import team.unnamed.redis.pubsub.RedisSubscriber;

import java.io.IOException;
import java.net.SocketAddress;

/**
 */
public interface RedisClient {

    String set(byte[] key, byte[] value);

    String set(String key, String value);

    String get(byte[] key);

    String get(String key);

    //#region Redis Publisher/Subscriber stuff
    /**
     * Sends a SUBSCRIBE command for the given {@code channels}
     * and waits for responses, it's a blocking operation.
     * Calls {@code subscriber} methods when a message is received.
     */
    void subscribe(RedisSubscriber subscriber, String... channels);
    //#endregion

    static RedisClient create(SocketAddress address) {
        try {
            return new RedisClientImpl(new RedisSocket(address));
        } catch (IOException e) {
            throw new RedisException("Error while connecting to redis server", e);
        }
    }

}
