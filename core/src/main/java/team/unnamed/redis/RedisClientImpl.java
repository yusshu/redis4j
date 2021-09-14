package team.unnamed.redis;

import team.unnamed.redis.io.RespWritable;

import java.io.IOException;

public class RedisClientImpl implements RedisClient {

    private final RedisSocket socket;

    protected RedisClientImpl(RedisSocket socket) {
        this.socket = socket;
    }

    private void sendCommand(RespWritable... args) {
        try {
            socket.getOutputStream().writeArray(args);
        } catch (IOException e) {
            throw new RedisException("Error occurred while" +
                    " sending command", e);
        }
    }

    private String readStringResponse() {
        socket.flush();

        Object response;
        try {
            response = Resp.readResponse(socket.getInputStream());
        } catch (IOException e) {
            throw new RedisException("Error occurred while "
                    + "reading command response");
        }
        if (response instanceof byte[]) {
            return new String((byte[]) response, Resp.CHARSET);
        }
        return response.toString();
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
        sendCommand(RedisCommands.SET, RespWritable.bytes(key), RespWritable.bytes(value));
        return readStringResponse();
    }

    @Override
    public String set(String key, String value) {
        return set(key.getBytes(Resp.CHARSET), value.getBytes(Resp.CHARSET));
    }

    @Override
    public String get(byte[] key) {
        sendCommand(RedisCommands.GET, RespWritable.bytes(key));
        return readStringResponse();
    }

    @Override
    public String get(String key) {
        return get(key.getBytes(Resp.CHARSET));
    }
}
