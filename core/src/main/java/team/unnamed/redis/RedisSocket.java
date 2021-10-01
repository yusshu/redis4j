package team.unnamed.redis;

import team.unnamed.redis.io.RespInputStream;
import team.unnamed.redis.io.RespOutputStream;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class RedisSocket implements Flushable, Closeable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final SocketAddress address;
    private final Socket socket;

    private final RespInputStream inputStream;
    private final RespOutputStream outputStream;

    public RedisSocket(SocketAddress address) throws IOException {
        this.address = address;
        this.socket = connect(address);

        this.inputStream = new RespInputStream(socket.getInputStream(), DEFAULT_BUFFER_SIZE);
        this.outputStream = new RespOutputStream(socket.getOutputStream(), DEFAULT_BUFFER_SIZE);
    }

    public SocketAddress getAddress() {
        return address;
    }

    public Socket getSocket() {
        return socket;
    }

    public RespInputStream getInputStream() {
        return inputStream;
    }

    public RespOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void flush() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw new RedisException(e);
        }
    }

    @Override
    public void close() {
        try {
            outputStream.flush();
            socket.close();
        } catch (IOException e) {
            throw new RedisException(e);
        }
    }

    private static Socket connect(SocketAddress address) throws IOException {
        Socket socket = new Socket();

        socket.setReuseAddress(true);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.setSoLinger(true, 0);

        socket.connect(address);
        return socket;
    }

}
