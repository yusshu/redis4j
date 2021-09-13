package team.unnamed.redis;

import java.io.IOException;
import java.net.SocketAddress;

/**
 */
public interface RedisClient {

    String set(byte[] key, byte[] value);

    String set(String key, String value);

    String get(byte[] key);

    String get(String key);

    void execute(RedisOperation operation);

    static RedisClient create(SocketAddress address) {
        try {
            return new RedisClientImpl(new RedisSocket(address));
        } catch (IOException e) {
            throw new RedisException("Error while connecting to redis server", e);
        }
    }

}
