package team.unnamed.redis.datatype;

import team.unnamed.redis.RedisException;
import team.unnamed.redis.Resp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class for working with strings following the
 * RESP (REdis Serialization Protocol) specification
 */
public final class RespStrings {

    private RespStrings() {
    }

    /**
     * Writes the given {@code value} into the specified
     * {@code output} following the RESP specification, note
     * that this data type is UNSAFE, it can't contain \r or
     * \n since it doesn't specify a length, it just detects
     * \r and \n as the end
     * i.e.
     *   +Hello world\r\n
     *   +Ping!\r\n
     *   +Pong!\r\n
     * @throws IOException If write fails
     */
    public static void writeSimpleString(OutputStream output, String value) throws IOException {
        // simple string start
        output.write(Resp.SIMPLE_STRING_BYTE);

        // write actual string
        output.write(value.getBytes(Resp.CHARSET));
        Resp.writeTermination(output);
    }

    /**
     * Writes the given {@code value} into the specified
     * {@code output} following the RESP specification
     * i.e.
     *   $3\r\nabc\r\n
     *   $11\r\nHello World\r\n
     *   $0\r\n\r\n
     * @throws IOException If write fails
     */
    public static void writeBulkString(OutputStream output, byte[] value) throws IOException {
        // bulk data start
        output.write(Resp.BULK_STRING_BYTE);

        // write data length
        RespIntegers.writeIntAsString(output, value.length);
        Resp.writeTermination(output);

        // write the actual data
        output.write(value);
        Resp.writeTermination(output);
    }

    /**
     * Variation of {@link RespStrings#writeBulkString}, it just doesn't write
     * the string data termination and the length is '-1', note that an
     * empty bulk string and a null bulk string are totally different.
     * It writes: $-1\r\n
     * @throws IOException If write fails
     */
    public static void writeNullBulkString(OutputStream output) throws IOException {
        // bulk string start
        output.write(Resp.BULK_STRING_BYTE);

        // write length (-1)
        output.write(Resp.SCRIPT_BYTE);
        output.write((byte) '1');

        // write termination
        Resp.writeTermination(output);
    }

    public static String readSimpleString(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        int data = input.read();
        while (true) {
            if (data == -1) {
                throw new EOFException("Found end when reading simple string");
            } else if (data == Resp.CARRIAGE_RETURN) {
                int next = input.read();
                if (next == Resp.LINE_FEED) {
                    break;
                } else {
                    builder.append((char) data);
                    data = next;
                    continue;
                }
            }

            builder.append((char) data);
            data = input.read();
        }
        return builder.toString();
    }

    public static byte[] readBulkString(InputStream input) throws IOException {
        int length = RespIntegers.readInteger(input);

        // null bulk string
        if (length == -1) {
            return null;
        }

        byte[] data = new byte[length];
        int read = 0;

        while (read < length) {
            int size = input.read(data, read, (length - read));
            if (size == -1) {
                throw new EOFException("Found EOF when" +
                        " reading bulk string");
            }
            read += size;
        }

        if (input.read() != Resp.CARRIAGE_RETURN || input.read() != Resp.LINE_FEED) {
            throw new RedisException("Invalid bulk string end, expected \\r\\n");
        }

        return data;
    }

}
