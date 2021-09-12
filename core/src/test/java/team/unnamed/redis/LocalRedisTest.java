package team.unnamed.redis;

import org.junit.jupiter.api.BeforeAll;

import java.net.InetSocketAddress;

public abstract class LocalRedisTest {

    protected static RedisClient client;

    @BeforeAll
    public static void initClient() {
        client = RedisClient.create(new InetSocketAddress("127.0.0.1", 6379));
    }

}
