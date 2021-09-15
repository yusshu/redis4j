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

    public byte readByte() throws IOException {
        fill();
        return buffer[cursor++];
    }

    @Override
    public int read() throws IOException {
        return readByte();
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        fill(); // fill buffer

        // how many bytes will be read
        int len = Math.min(
                // the remaining bytes
                limit - cursor,
                // or the actual array length
                length
        );

        System.arraycopy(buffer, cursor, bytes, offset, len);
        cursor += len;
        return len;
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
        byte start = readByte();

        if (start == -1) {
            throw new EOFException("Found end when reading integer");
        }

        boolean negative = start == Resp.SCRIPT_BYTE;
        int value = 0;
        int b = negative ? readByte() : start;

        while (true) {
            if (b != Resp.CARRIAGE_RETURN) {
                value = value * 10 + b - '0';
                b = readByte();
                continue;
            } else if (readByte() != Resp.LINE_FEED) {
                throw new RedisException("Invalid integer end");
            }
            break;
        }

        return negative ? -value : value;
    }

    @Override
    public String readSimpleString() throws IOException {
        StringBuilder builder = new StringBuilder();
        fill();
        byte data = buffer[cursor++];

        while (true) {
            if (data == Resp.CARRIAGE_RETURN) {
                fill();
                byte next = buffer[cursor++];
                if (next == Resp.LINE_FEED) {
                    break;
                } else {
                    builder.append((char) data);
                    data = next;
                    continue;
                }
            }

            builder.append((char) data);
            fill();
            data = buffer[cursor++];
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
        int offset = 0;

        while (offset < length) {
            int read = read(data, offset, length - offset);
            if (read == -1) {
                throw new EOFException();
            }
            offset += read;
        }

        readByte();
        readByte();

        return data;
    }

}
