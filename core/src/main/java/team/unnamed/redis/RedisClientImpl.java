package team.unnamed.redis;

import java.io.IOException;

public class RedisClientImpl implements RedisClient {

    private final RedisSocket socket;

    protected RedisClientImpl(RedisSocket socket) {
        this.socket = socket;
    }

    @Override
    public String set(byte[] key, byte[] value) {
        try {
            Resp.writeArray(
                    socket.getOutputStream(),
                    RedisCommands.SET,
                    Writable.bytes(key),
                    Writable.bytes(value)
            );
        } catch (IOException e) {
            throw new RedisException("Error occurred while sending command", e);
        }
        try {
            return Resp.readResponse(socket.getInputStream())
                    .toString();
        } catch (IOException e) {
            throw new RedisException("Error occurred while reading command response", e);
        }
    }

    @Override
    public String set(String key, String value) {
        return set(key.getBytes(Resp.CHARSET), value.getBytes(Resp.CHARSET));
    }

    @Override
    public String get(byte[] key) {
        try {
            Resp.writeArray(
                    socket.getOutputStream(),
                    RedisCommands.GET,
                    Writable.bytes(key)
            );
            return new String(((byte[]) Resp.readResponse(socket.getInputStream())), Resp.CHARSET);
        } catch (IOException e) {
            throw new RedisException("Error occurred while executing command");
        }
    }

    @Override
    public String get(String key) {
        return get(key.getBytes(Resp.CHARSET));
    }
}
