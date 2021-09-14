package team.unnamed.redis;

import team.unnamed.redis.datatype.RespArrays;

import java.io.IOException;

public class RedisClientImpl implements RedisClient {

    private final RedisSocket socket;

    protected RedisClientImpl(RedisSocket socket) {
        this.socket = socket;
    }

    private void sendCommand(Writable... args) {
        try {
            RespArrays.writeArray(socket.getOutputStream(), args);
        } catch (IOException e) {
            throw new RedisException("Error occurred while" +
                    " sending command", e);
        }
    }

    private String readStringResponse() {

        try {
            socket.flush();
        } catch (IOException e) {
            throw new RedisException("Error occurred while" +
                    " flushing output stream");
        }

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
        sendCommand(RedisCommands.SET, Writable.bytes(key), Writable.bytes(value));
        return readStringResponse();
    }

    @Override
    public String set(String key, String value) {
        return set(key.getBytes(Resp.CHARSET), value.getBytes(Resp.CHARSET));
    }

    @Override
    public String get(byte[] key) {
        sendCommand(RedisCommands.GET, Writable.bytes(key));
        return readStringResponse();
    }

    @Override
    public String get(String key) {
        return get(key.getBytes(Resp.CHARSET));
    }
}
