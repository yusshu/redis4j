package team.unnamed.redis;

import team.unnamed.redis.datatype.RespArrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RedisOperation implements Writable {

    private final byte[] bytes;

    public RedisOperation(Writable writable) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writable.write(output);
        this.bytes = output.toByteArray();
    }

    @Override
    public void write(OutputStream output) throws IOException {
        output.write(bytes);
    }

    public static RedisOperation get(String key) throws IOException {
        return new RedisOperation(output -> RespArrays.writeArray(
                output,
                RedisCommands.GET,
                Writable.bulkString(key)
        ));
    }

    public static RedisOperation set(String key, String value) throws IOException {
        return new RedisOperation(output -> RespArrays.writeArray(
                output,
                RedisCommands.SET,
                Writable.bulkString(key),
                Writable.bulkString(value)
        ));
    }

}
