package team.unnamed.redis.io;

import team.unnamed.redis.RedisException;
import team.unnamed.redis.Resp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BufferedRespInputStream extends RespInputStream {

    private final byte[] buffer;
    private int cursor;
    private int limit;

    public BufferedRespInputStream(InputStream in, int bufferLength) {
        super(in);
        this.buffer = new byte[bufferLength];
    }

    private void fill() throws IOException {
        if (cursor >= limit) {
            limit = in.read(buffer);
            cursor = 0;
            if (limit == -1) {
                throw new EOFException();
            }
        }
    }

    protected byte bufferRead() throws IOException {
        fill();
        return buffer[cursor++];
    }

    @Override
    public Object[] readArray() throws IOException {
        int length = readInt();

        // null array
        if (length == -1) {
            return null;
        }

        Object[] value = new Object[length];
        for (int i = 0; i < length; i++) {
            value[i] = Resp.readResponse(this);
        }
        return value;
    }

    @Override
    public int readInt() throws IOException {
        int start = read();

        if (start == -1) {
            throw new EOFException("Found end when reading integer");
        }

        boolean negative = start == Resp.SCRIPT_BYTE;
        int value = 0;
        int b = negative ? read() : start;

        while (true) {
            if (b != Resp.CARRIAGE_RETURN) {
                value = value * 10 + b - '0';
                b = read();
                continue;
            } else if (read() != Resp.LINE_FEED) {
                throw new RedisException("Invalid integer end");
            }
            break;
        }

        return negative ? -value : value;
    }

    @Override
    public String readSimpleString() throws IOException {
        StringBuilder builder = new StringBuilder();
        int data = bufferRead();
        while (true) {
            if (data == -1) {
                throw new EOFException("Found end when reading simple string");
            } else if (data == Resp.CARRIAGE_RETURN) {
                int next = bufferRead();
                if (next == Resp.LINE_FEED) {
                    break;
                } else {
                    builder.append((char) data);
                    data = next;
                    continue;
                }
            }

            builder.append((char) data);
            data = bufferRead();
        }
        return builder.toString();
    }

    @Override
    public byte[] readBulkString() throws IOException {
        int length = readInt();

        // null bulk string
        if (length == -1) {
            return null;
        }

        byte[] data = new byte[length];
        int read = 0;

        while (read < length) {
            int size = read(data, read, (length - read));
            if (size == -1) {
                throw new EOFException("Found EOF when" +
                        " reading bulk string");
            }
            read += size;
        }

        if (bufferRead() != Resp.CARRIAGE_RETURN || bufferRead() != Resp.LINE_FEED) {
            throw new RedisException("Invalid bulk string end, expected \\r\\n");
        }

        return data;
    }

}
