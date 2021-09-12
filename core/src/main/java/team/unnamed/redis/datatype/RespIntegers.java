package team.unnamed.redis.datatype;

import team.unnamed.redis.RedisException;
import team.unnamed.redis.Resp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class for working with integers following the
 * RESP (REdis Serialization Protocol) specification
 */
public final class RespIntegers {

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

    private RespIntegers() {
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
     * {@code output} following the RESP specification
     * i.e.
     *   :10000\r\n
     *   :-500\r\n
     * @throws IOException If write fails
     */
    public static void writeInteger(OutputStream output, int value) throws IOException {
        // TODO: Specification says '64 bit' integer but we're using a 32 bit integer hmm
        // integer start
        output.write(Resp.INTEGER_BYTE);

        // integer write
        if (value < 0) {
            output.write(Resp.SCRIPT_BYTE);
            value = -value;
        }
        writeIntAsString(output, value);
        Resp.writeTermination(output);
    }

    public static int readInteger(InputStream input) throws IOException {
        int start = input.read();

        if (start == -1) {
            throw new EOFException("Found end when reading integer");
        }

        boolean negative = start == Resp.SCRIPT_BYTE;
        int value = 0;
        int b = negative ? input.read() : start;

        while (true) {
            if (b != Resp.CARRIAGE_RETURN) {
                value = value * 10 + b - '0';
                b = input.read();
                continue;
            } else if (input.read() != Resp.LINE_FEED) {
                throw new RedisException("Invalid integer end");
            }
            break;
        }

        return negative ? -value : value;
    }

}
