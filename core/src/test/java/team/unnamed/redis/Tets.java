package team.unnamed.redis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Tets {

    @Test
    public void tets() throws IOException {
        SocketAddress address = new InetSocketAddress("127.0.0.1", 6379);
        RedisClient client = RedisClient.create(address);

        System.out.println(client.set("sex", "please"));
        System.out.println(client.get("sex"));
    }

}
