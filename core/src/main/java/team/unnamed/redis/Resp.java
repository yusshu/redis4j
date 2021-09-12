package team.unnamed.redis;

import team.unnamed.redis.datatype.RespIntegers;
import team.unnamed.redis.datatype.RespStrings;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class containing information about RESP
 * (REdis Serialization Protocol)
 * See <a href="https://redis.io/topics/protocol">
 *     https://redis.io/topics/protocol</a>
 */
public final class Resp {

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    // bytes for some used characters
    public static final byte SIMPLE_STRING_BYTE = '+';
    public static final byte ERROR_BYTE = '-';
    public static final byte INTEGER_BYTE = ':';
    public static final byte BULK_STRING_BYTE = '$';
    public static final byte ARRAY_BYTE = '*';
    public static final byte SCRIPT_BYTE = '-';

    public static final byte CARRIAGE_RETURN = '\r';
    public static final byte LINE_FEED = '\n';

    private Resp() {
    }

    public static Object readResponse(InputStream input) throws IOException {
        int code = input.read();
        if (code == -1) {
            throw new EOFException("Found EOF when reading response");
        }

        switch (code) {
            case ERROR_BYTE:
                throw new RedisException(RespStrings.readSimpleString(input));
            case SIMPLE_STRING_BYTE:
                return RespStrings.readSimpleString(input);
            case INTEGER_BYTE:
                return RespIntegers.readInteger(input);
            case BULK_STRING_BYTE:
                return RespStrings.readBulkString(input);
            case ARRAY_BYTE:
                return readArray(input);
            default: {
                throw new RedisException("Unknown response byte: "
                        + ((char) code));
            }
        }
    }

    public static Object[] readArray(InputStream input) throws IOException {
        int length = RespIntegers.readInteger(input);

        // null array
        if (length == -1) {
            return null;
        }

        Object[] value = new Object[length];
        for (int i = 0; i < length; i++) {
            value[i] = readResponse(input);
        }
        return value;
    }

    /**
     * Simply writes the termination bytes to the given {@code output},
     * RESP uses CRLF for this
     *
     * <strong>Note that the given {@code output} won't be closed
     * when writing the termination</strong>
     *
     * @throws IOException If write fails
     */
    public static void writeTermination(OutputStream output) throws IOException {
        output.write(CARRIAGE_RETURN);
        output.write(LINE_FEED);
    }

    /**
     * Writes the given {@code array} into the provided
     * {@code output} following the RESP specification.
     * i.e.
     *   *1\r\n:10\r\n
     *   *0\r\n
     *   *2\r\n+Hello\r\n+World\r\n
     * @throws IOException If write fails
     */
    public static void writeArray(
            OutputStream output,
            Writable... array
    ) throws IOException {
        // array start
        output.write(ARRAY_BYTE);

        // array length write
        RespIntegers.writeIntAsString(output, array.length);
        writeTermination(output);

        // element write
        for (Writable writer : array) {
            writer.write(output);
        }
    }

    /**
     * Writes a null array into the given {@code output}
     * following the RESP specification.
     * It writes:
     *      *-1\r\n
     * Note that an empty array and a null array are totally
     * different
     * @throws IOException If write fails
     */
    public static void writeNullArray(OutputStream output) throws IOException {
        // array start
        output.write(ARRAY_BYTE);

        // write length (-1)
        output.write(SCRIPT_BYTE);
        output.write((byte) '1');

        // termination
        writeTermination(output);
    }

}
