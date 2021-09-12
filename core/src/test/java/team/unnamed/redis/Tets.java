package team.unnamed.redis;

import org.junit.jupiter.api.Test;
import team.unnamed.redis.connection.RedisSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Tets {

    @Test
    public void tets() throws IOException {
        SocketAddress address = new InetSocketAddress("127.0.0.1", 6379);
        RedisSocket socket = new RedisSocket(address);

        socket.set("test", "test");
    }

}
