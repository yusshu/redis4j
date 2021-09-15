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
    void subscribe(RedisSubscriber subscriber, String... channels);
    //#endregion

    void execute(RedisOperation operation);

    static RedisClient create(SocketAddress address) {
        try {
            return new RedisClientImpl(new RedisSocket(address));
        } catch (IOException e) {
            throw new RedisException("Error while connecting to redis server", e);
        }
    }

}
