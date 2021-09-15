package team.unnamed.redis.io;

import team.unnamed.redis.Resp;

import java.io.IOException;
import java.io.OutputStream;

public class BufferedRespOutputStream extends RespOutputStream {

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

    private final byte[] buffer;
    private int cursor;

    public BufferedRespOutputStream(OutputStream out, int bufferLength) {
        super(out);
        this.buffer = new byte[bufferLength];
    }

    protected void flushBuffer() throws IOException {
        if (cursor > 0) {
            out.write(buffer, 0, cursor);
            cursor = 0;
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (cursor >= buffer.length) {
            flushBuffer();
        }
        buffer[cursor++] = (byte) b;
    }

    @Override
    public void write(byte[] bytes, int offset, int len) throws IOException {
        if (len >= buffer.length) {
            flushBuffer();
            out.write(bytes, offset, len);
        } else {
            if (len > buffer.length - cursor) {
                flushBuffer();
            }

            System.arraycopy(bytes, offset, buffer, cursor, len);
            cursor += len;
        }
    }

    private void writeTermination() throws IOException {
        if (2 >= buffer.length) {
            flushBuffer();
        }
        buffer[cursor++] = Resp.CARRIAGE_RETURN;
        buffer[cursor++] = Resp.LINE_FEED;
    }

    /**
     * Writes the given {@code value} integer into the given
     * {@code output} as a string. The given {@code value}
     * must not be negative or weird things may happen
     * @throws IOException If write fails
     */
    private void writeIntAsString(int value) throws IOException {

        // compute string size for the given value
        // taken from Integer#stringSize(int)
        int size = 0;
        while (value > SIZE_TABLE[size]) {
            size++;
        }
        size++;

        if (size >= buffer.length - cursor) {
            flushBuffer();
        }

        int pos = cursor + size;

        // write the integer as string into 'valueBytes', because
        // we can't read the number digits from left to right
        // (code taken from Integer#getChars)
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

        cursor += size;
    }

    @Override
    public void writeInt(int value) throws IOException {
        // TODO: Specification says '64 bit' integer but we're using a 32 bit integer hmm
        // integer start
        write(Resp.INTEGER_BYTE);

        // integer write
        if (value < 0) {
            write(Resp.SCRIPT_BYTE);
            value = -value;
        }
        writeIntAsString(value);
        writeTermination();
    }

    @Override
    public void writeSimpleString(String value) throws IOException {
        // simple string start
        write(Resp.SIMPLE_STRING_BYTE);

        // write actual string
        write(value.getBytes(Resp.CHARSET));
        writeTermination();
    }

    @Override
    public void writeBulkString(byte[] value) throws IOException {
        // bulk data start
        write(Resp.BULK_STRING_BYTE);

        // write data length
        writeIntAsString(value.length);
        writeTermination();

        // write the actual data
        write(value);
        writeTermination();
    }

    @Override
    public void writeNullBulkString() throws IOException {
        // bulk string start
        write(Resp.BULK_STRING_BYTE);

        // write length (-1)
        write(Resp.SCRIPT_BYTE);
        write((byte) '1');

        // write termination
        writeTermination();
    }

    @Override
    public void writeArray(RespWritable... array) throws IOException {
        // array start
        write(Resp.ARRAY_BYTE);

        // array length write
        writeIntAsString(array.length);
        writeTermination();

        // element write
        for (RespWritable writer : array) {
            writer.write(this);
        }
    }

    @Override
    public void writeArray(byte[]... array) throws IOException {
        // array start
        write(Resp.ARRAY_BYTE);

        // array length write
        writeIntAsString(array.length);
        writeTermination();

        // element write
        for (byte[] element : array) {
            write(element);
        }
    }

    @Override
    public void writeNullArray() throws IOException {
        // array start
        write(Resp.ARRAY_BYTE);

        // write length (-1)
        write(Resp.SCRIPT_BYTE);
        write((byte) '1');

        // termination
        writeTermination();
    }

    @Override
    public void writeCommand(RespWritable command, byte[]... args) throws IOException {
        // array start
        write(Resp.ARRAY_BYTE);

        // write length
        writeIntAsString(args.length + 1);
        writeTermination();

        command.write(this);

        // element write
        for (byte[] element : args) {
            writeBulkString(element);
        }
    }

    @Override
    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }

}
