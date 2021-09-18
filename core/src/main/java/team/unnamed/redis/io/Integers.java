package team.unnamed.redis.io;

/**
 * Utility class for working with integers
 * <b>internal use only!</b>
 */
public final class Integers {

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

    private Integers() {
    }

    /**
     * Computes the length for the string representation for
     * the given {@code value} (decimal), note that the given
     * {@code value} <b>must not be negative</b>
     * @see Integers#DIGITS
     */
    public static int getStringSize(int value) {
        // compute string size for the given value
        // taken from Integer#stringSize(int)
        int size = 0;
        while (value > SIZE_TABLE[size]) {
            size++;
        }
        return ++size;
    }

    /**
     * Computes the characters for the given {@code value} and
     * writes them into the given {@code buffer}, using the given
     * {@code off} as offset and assuming that the given {@code size}
     * is correct (given by Integers#getStringSize).
     *
     * <b>Note that the given {@code value} must not be negative</b>
     *
     * @see Integers#getStringSize
     */
    public static void getChars(int value, byte[] buffer, int off, int size) {

        // (code taken from Integer#getChars)

        // compute the last position for the characters, since we
        // are reading from right to left, this position will be
        // decremented later
        int pos = off + size;

        int q;
        int r;
        while (value >= TWO_BYTES_BITS) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buffer[--pos] = DIGITS_ONES[r];
            buffer[--pos] = DIGITS_TENS[r];
        }

        do {
            q = (value * 52429) >>> 19;
            r = value - ((q << 3) + (q << 1));
            buffer[--pos] = DIGITS[r];
        } while ((value = q) != 0);
    }

}
