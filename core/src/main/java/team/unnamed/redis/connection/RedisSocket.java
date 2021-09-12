package team.unnamed.redis.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class RedisSocket {

    private final SocketAddress address;
    private final Socket socket;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public RedisSocket(SocketAddress address) throws IOException {
        this.address = address;
        this.socket = connect(address);

        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
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
