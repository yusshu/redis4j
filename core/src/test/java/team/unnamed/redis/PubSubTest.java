package team.unnamed.redis;

import org.junit.jupiter.api.Test;
import team.unnamed.redis.pubsub.RedisSubscriber;

public class PubSubTest extends LocalRedisTest {

    @Test
    public void test() {
        // TODO: idk how to test this hmm
        client.subscribe(new RedisSubscriber() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("received: '" + message + "' from channel: '" + channel + "'");
            }
        }, "test1", "test2");
    }

}
