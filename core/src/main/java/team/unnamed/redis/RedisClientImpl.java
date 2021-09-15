package team.unnamed.redis;

import team.unnamed.redis.io.RespWritable;
import team.unnamed.redis.pubsub.RedisSubscriber;
import team.unnamed.redis.pubsub.BlockingRedisSubscription;
import team.unnamed.redis.util.Strings;

import java.io.IOException;
import java.util.List;

public class RedisClientImpl implements RedisClient {

    private final RedisSocket socket;

    protected RedisClientImpl(RedisSocket socket) {
        this.socket = socket;
    }

    private void sendCommand(RespWritable command, byte[]... args) {
        try {
            socket.getOutputStream().writeCommand(command, args);
        } catch (IOException e) {
            throw new RedisException("Error occurred while" +
                    " sending command", e);
        }
    }

    public String readStringResponse() {
        try {
            return new String((byte[]) socket.getInputStream().readNext(), Resp.CHARSET);
        } catch (IOException e) {
            throw new RedisException(e);
        }
    }

    public Object[] readArrayResponse() {
        try {
            return (Object[]) socket.getInputStream().readNext();
        } catch (IOException e) {
            throw new RedisException(e);
        }
    }

    @Override
    public void execute(RedisOperation operation) {
        try {
            operation.write(socket.getOutputStream());
        } catch (IOException e) {
            throw new RedisException("Error occurred while"
                    + " executing operation");
        }
    }

    @Override
    public String set(byte[] key, byte[] value) {
        sendCommand(RedisCommands.SET, key, value);
        socket.flush();
        return readStringResponse();
    }

    @Override
    public String set(String key, String value) {
        return set(Strings.encode(key), Strings.encode(value));
    }

    @Override
    public String get(byte[] key) {
        sendCommand(RedisCommands.GET, key);
        socket.flush();
        return readStringResponse();
    }

    @Override
    public String get(String key) {
        return get(key.getBytes(Resp.CHARSET));
    }

    @Override
    public void subscribe(RedisSubscriber subscriber, String... channels) {
        sendCommand(RedisCommands.SUBSCRIBE, Strings.encodeArray(channels));
        socket.flush();

        // blocking operation!
        new BlockingRedisSubscription(this, subscriber).run();
    }

}
