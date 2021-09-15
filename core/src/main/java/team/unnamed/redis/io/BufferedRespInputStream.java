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
    public Object readNext() throws IOException {
        byte code = readByte();
        switch (code) {
            case Resp.ERROR_BYTE:
                throw new RedisException(new String(readSimpleString()));
            case Resp.SIMPLE_STRING_BYTE:
                return readSimpleString();
            case Resp.INTEGER_BYTE:
                return readInt();
            case Resp.BULK_STRING_BYTE:
                return readBulkString();
            case Resp.ARRAY_BYTE:
                return readArray();
            default: {
                throw new RedisException("Unknown response byte: "
                        + ((char) code));
            }
        }
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
            value[i] = readNext();
        }
        return value;
    }

    @Override
    public int readInt() throws IOException {
        fill();

        boolean negative = buffer[cursor] == '-';
        if (negative) {
            ++cursor;
        }

        int value = 0;
        while (true) {
            fill();
            int b = buffer[cursor++];
            if (b == Resp.CARRIAGE_RETURN) {
                fill();
                if (buffer[cursor++] != Resp.LINE_FEED) {
                    throw new RedisException("Unexpected char");
                }
                break;
            } else {
                value = value * 10 + b - '0';
            }
        }

        return (negative ? -value : value);
    }

    @Override
    public byte[] readSimpleString() throws IOException {
        fill();

        int pos = cursor;

        while (true) {
            if (buffer[pos++] == Resp.CARRIAGE_RETURN
                    && buffer[pos++] == Resp.LINE_FEED) {
                break;
            }
        }

        byte[] data = new byte[pos - cursor - 2];
        System.arraycopy(buffer, cursor, data, 0, data.length);
        cursor = pos;
        return data;
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
