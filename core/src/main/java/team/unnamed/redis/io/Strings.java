package team.unnamed.redis.io;

import team.unnamed.redis.Resp;

/**
 * Utility class for working with {@link String}
 * instances, internal use only!
 */
public final class Strings {

    private Strings() {
    }

    /**
     * Converts the given {@code string} to a
     * byte array using the charset specified
     * at {@link Resp#CHARSET}
     */
    public static byte[] encode(String string) {
        return string.getBytes(Resp.CHARSET);
    }

    /**
     * Converts the given string array to a byte
     * matrix using the charset specified at
     * {@link Resp#CHARSET}
     */
    public static byte[][] encodeArray(String... strings) {
        int len = strings.length;
        byte[][] data = new byte[len][];
        for (int i = 0; i < len; i++) {
            data[i] = encode(strings[i]);
        }
        return data;
    }

    /**
     * Converts the given {@code bytes} to an
     * actual {@link String} using the charset
     * specified at {@link Resp#CHARSET}
     */
    public static String decode(byte[] bytes) {
        return new String(bytes, Resp.CHARSET);
    }

}
