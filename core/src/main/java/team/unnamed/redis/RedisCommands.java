package team.unnamed.redis;

import team.unnamed.redis.io.Integers;

/**
 * Utility class containing all the
 * existing redis commands
 */
public final class RedisCommands {

    public static final byte[]
            COPY = command("COPY"),
            DEL = command("DEL"),
            ECHO = command("ECHO"),
            EXISTS = command("EXISTS"),
            EXPIRE = command("EXPIRE"),
            GET = command("GET"),
            KEYS = command("KEYS"),
            PING = command("PING"),
            QUIT = command("QUIT"),
            PUBLISH = command("PUBLISH"),
            SET = command("SET"),
            SUBSCRIBE = command("SUBSCRIBE");

    private RedisCommands() {
    }

    private static byte[] command(String name) {
        byte[] string = name.getBytes(Resp.CHARSET);

        int len = string.length;
        int size = Integers.getStringSize(len);
        int cursor = 0;

        byte[] result = new byte[string.length + size + 5];

        result[cursor++] = Resp.BULK_STRING_BYTE;

        // write length
        Integers.getChars(len, result, cursor, size);
        cursor += size;

        result[cursor++] = Resp.CARRIAGE_RETURN;
        result[cursor++] = Resp.LINE_FEED;

        // write string
        System.arraycopy(string, 0, result, cursor, string.length);
        cursor += string.length;

        result[cursor++] = Resp.CARRIAGE_RETURN;
        result[cursor] = Resp.LINE_FEED;

        return result;
    }

}
