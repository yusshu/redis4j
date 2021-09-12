package team.unnamed.redis;

import org.junit.jupiter.api.Test;
import team.unnamed.redis.connection.RedisSocket;
import team.unnamed.redis.protocol.RedisCommands;
import team.unnamed.redis.protocol.Resp;
import team.unnamed.redis.serialize.RespWriter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Tets {

    @Test
    public void tets() throws IOException {
        SocketAddress address = new InetSocketAddress("127.0.0.1", 6379);
        RedisSocket socket = new RedisSocket(address);

        Resp.writeArray(
                socket.getOutputStream(),
                RedisCommands.PING,
                RespWriter.bulkString("Que")
        );

        System.out.println("====== Response Start ======");

        int d;
        while ((d = socket.getInputStream().read()) != -1) {
            System.out.println("char = " + d);
        }
        System.out.println();
        System.out.println("======= Response End =======");
    }

}
