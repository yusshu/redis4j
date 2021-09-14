package team.unnamed.redis;

import team.unnamed.redis.io.BufferedRespOutputStream;
import team.unnamed.redis.io.RespOutputStream;
import team.unnamed.redis.io.RespWritable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RedisOperation implements RespWritable {

    private final byte[] bytes;

    public RedisOperation(RespWritable writable) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // TODO: replace with a direct resp output stream!
        writable.write(new BufferedRespOutputStream(output, 1024));
        this.bytes = output.toByteArray();
    }

    @Override
    public void write(RespOutputStream output) throws IOException {
        output.write(bytes);
    }

    public static RedisOperation get(String key) throws IOException {
        return new RedisOperation(output -> output.writeArray(
                RedisCommands.GET,
                RespWritable.bulkString(key)
        ));
    }

    public static RedisOperation set(String key, String value) throws IOException {
        return new RedisOperation(output -> output.writeArray(
                RedisCommands.SET,
                RespWritable.bulkString(key),
                RespWritable.bulkString(value)
        ));
    }

}
