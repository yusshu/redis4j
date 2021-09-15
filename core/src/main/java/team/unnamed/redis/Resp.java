package team.unnamed.redis;

import team.unnamed.redis.io.RespInputStream;

import java.io.EOFException;
import java.io.IOException;
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

}
