package team.unnamed.redis;

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

    /**
     * Size table for integers, to check the string length of a number.
     * Taken from {@link Integer} class
     */
    private static final int[] SIZE_TABLE = {
            9, 99, 999, 9999, 99999, 999999, 9999999,
            99999999, 999999999, Integer.MAX_VALUE
    };

    private static final byte[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };
    private static final byte[] DIGITS_TENS = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
    };

    private static final byte[] DIGITS_ONES = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static final int TWO_BYTES_BITS = 1 << 16;

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
                throw new RedisException(readSimpleString(input));
            case SIMPLE_STRING_BYTE:
                return readSimpleString(input);
            case INTEGER_BYTE:
                return readInteger(input);
            case BULK_STRING_BYTE:
                return readBulkString(input);
            case ARRAY_BYTE:
                return readArray(input);
            default: {
                throw new RedisException("Unknown response byte: "
                        + ((char) code));
            }
        }
    }

    public static String readSimpleString(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        int data = input.read();
        while (true) {
            if (data == -1) {
                throw new EOFException("Found end when reading simple string");
            } else if (data == CARRIAGE_RETURN) {
                int next = input.read();
                if (next == LINE_FEED) {
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

    public static int readInteger(InputStream input) throws IOException {
        int start = input.read();

        if (start == -1) {
            throw new EOFException("Found end when reading integer");
        }

        boolean negative = start == SCRIPT_BYTE;
        int value = 0;
        int b = negative ? input.read() : start;

        while (true) {
            if (b != CARRIAGE_RETURN) {
                value = value * 10 + b - '0';
                b = input.read();
                continue;
            } else if (input.read() != LINE_FEED) {
                throw new RedisException("Invalid integer end");
            }
            break;
        }

        return negative ? -value : value;
    }

    public static byte[] readBulkString(InputStream input) throws IOException {
        int length = readInteger(input);

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

        if (input.read() != CARRIAGE_RETURN || input.read() != LINE_FEED) {
            throw new RedisException("Invalid bulk string end, expected \\r\\n");
        }

        return data;
    }

    public static Object[] readArray(InputStream input) throws IOException {
        int length = readInteger(input);

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
     * Writes the given {@code value} integer into the given
     * {@code output} as a string. The given {@code value}
     * must not be negative or weird things may happen
     * @throws IOException If write fails
     */
    public static void writeIntAsString(OutputStream output, int value)
            throws IOException {

        // compute string size for the given value
        // taken from Integer#stringSize(int)
        int size = 0;
        while (value > SIZE_TABLE[size]) {
            size++;
        }
        size++;

        byte[] valueBytes = new byte[size];
        int pos = size;

        // write the integer as string into 'valueBytes', because
        // we can't read the number digits from left to right
        // (code taken from Integer#getChars)
        int q;
        int r;
        while (value >= TWO_BYTES_BITS) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            valueBytes[--pos] = DIGITS_ONES[r];
            valueBytes[--pos] = DIGITS_TENS[r];
        }

        do {
            q = (value * 52429) >>> 19;
            r = value - ((q << 3) + (q << 1));
            valueBytes[--pos] = DIGITS[r];
        } while ((value = q) != 0);

        // write the number bytes
        output.write(valueBytes);
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
        output.write(SIMPLE_STRING_BYTE);

        // write actual string
        output.write(value.getBytes(CHARSET));
        writeTermination(output);
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
        output.write(BULK_STRING_BYTE);

        // write data length
        writeIntAsString(output, value.length);
        writeTermination(output);

        // write the actual data
        output.write(value);
        writeTermination(output);
    }

    /**
     * Variation of {@link Resp#writeBulkString}, it just doesn't write
     * the string data termination and the length is '-1', note that an
     * empty bulk string and a null bulk string are totally different.
     * It writes: $-1\r\n
     * @throws IOException If write fails
     */
    public static void writeNullBulkString(OutputStream output) throws IOException {
        // bulk string start
        output.write(BULK_STRING_BYTE);

        // write length (-1)
        output.write(SCRIPT_BYTE);
        output.write((byte) '1');

        // write termination
        writeTermination(output);
    }

    /**
     * Writes the given {@code value} into the specified
     * {@code output} following the RESP specification
     * i.e.
     *   :10000\r\n
     *   :-500\r\n
     * @throws IOException If write fails
     */
    public static void writeInteger(OutputStream output, int value) throws IOException {
        // TODO: Specification says '64 bit' integer but we're using a 32 bit integer hmm
        // integer start
        output.write(INTEGER_BYTE);

        // integer write
        if (value < 0) {
            output.write(SCRIPT_BYTE);
            value = -value;
        }
        writeIntAsString(output, value);
        writeTermination(output);
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
        writeIntAsString(output, array.length);
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
