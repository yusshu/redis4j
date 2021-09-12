package team.unnamed.redis;

import team.unnamed.redis.datatype.RespArrays;
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
                return RespArrays.readArray(input);
            default: {
                throw new RedisException("Unknown response byte: "
                        + ((char) code));
            }
        }
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

}
