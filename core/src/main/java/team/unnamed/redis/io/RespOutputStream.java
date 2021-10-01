package team.unnamed.redis.io;

import team.unnamed.redis.Resp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstraction giving access to methods for
 * writing data following the Redis Serialization
 * Protocol
 * @author yusshu (Andre Roldan)
 */
public class RespOutputStream extends FilterOutputStream {

    private final byte[] buffer;
    private int cursor;

    public RespOutputStream(OutputStream out, int bufferLength) {
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

    @Override
    public void write(byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
    }

    @Override
    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }

    private void writeTermination() throws IOException {
        if (2 >= buffer.length) {
            flushBuffer();
        }
        buffer[cursor++] = Resp.CARRIAGE_RETURN;
        buffer[cursor++] = Resp.LINE_FEED;
    }

    private void writeNegativeOneAndTermination() throws IOException {
        if (4 >= buffer.length) {
            flushBuffer();
        }
        buffer[cursor++] = Resp.CARRIAGE_RETURN;
        buffer[cursor++] = Resp.LINE_FEED;
        buffer[cursor++] = Resp.SCRIPT_BYTE;
        buffer[cursor++] = Resp.ASCII_ONE_BYTE;
    }

    /**
     * Writes the given {@code value} integer into the given
     * {@code output} as a string. The given {@code value}
     * must not be negative or weird things may happen
     * @throws IOException If write fails
     */
    private void writeIntAsString(int value) throws IOException {
        int off = cursor;
        int size = Integers.getStringSize(value);
        if (size >= buffer.length - off) {
            flushBuffer();
        }
        Integers.getChars(value, buffer, off, size);
        cursor += size;
    }

    /**
     * Writes the given integer {@code value} into this
     * output stream following the RESP specification
     *
     * i.e.
     *   :10000\r\n
     *   :-500\r\n
     *
     * @throws IOException If write fails
     */
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

    /**
     * Writes the given string {@code value} into this output
     * stream following the RESP specification, note
     * that this data type is UNSAFE, it can't contain \r or
     * \n since it doesn't specify a length, read for simple
     * strings stops when CRLF is found
     * i.e.
     *   +Hello world\r\n
     *   +Ping!\r\n
     *   +Pong!\r\n
     * @throws IOException If write fails
     */
    public void writeSimpleString(String value) throws IOException {
        // simple string start
        write(Resp.SIMPLE_STRING_BYTE);

        // write actual string
        write(value.getBytes(Resp.CHARSET));
        writeTermination();
    }

    /**
     * Writes the given string {@code value} into this output
     * stream following the RESP specification
     * i.e.
     *   $3\r\nabc\r\n
     *   $11\r\nHello World\r\n
     *   $0\r\n\r\n
     * @throws IOException If write fails
     */
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

    /**
     * Variation of {@link RespOutputStream#writeBulkString}, it just
     * doesn't write the string data termination and the length is '-1',
     * note that an empty bulk string and a null bulk string are totally
     * different.
     * It writes: $-1\r\n
     * @throws IOException If write fails
     */
    public void writeNullBulkString() throws IOException {
        // bulk string start
        write(Resp.BULK_STRING_BYTE);
        writeNegativeOneAndTermination();
    }

    /**
     * Writes the given {@code array} into this output stream following
     * the RESP specification.
     * i.e.
     *   *1\r\n:10\r\n
     *   *0\r\n
     *   *2\r\n+Hello\r\n+World\r\n
     * @throws IOException If write fails
     */
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

    /**
     * Writes a null array into this output stream following the RESP
     * specification.
     * It writes:
     *      *-1\r\n
     * Note that an empty array and a null array are totally
     * different
     * @throws IOException If write fails
     */
    public void writeNullArray() throws IOException {
        write(Resp.ARRAY_BYTE);
        writeNegativeOneAndTermination();
    }

    /**
     * Writes the given {@code command} and {@code args} to the underlying
     * {@link OutputStream}. Similar to {@link RespOutputStream#writeArray},
     * but the arguments are written as bulk strings, so wrapping them in
     * bulk strings isn't necessary
     * @throws IOException If write fails
     */
    public void writeCommand(byte[] command, byte[]... args) throws IOException {
        // array start
        write(Resp.ARRAY_BYTE);

        // write length
        writeIntAsString(args.length + 1);
        writeTermination();

        write(command);

        // element write
        for (byte[] element : args) {
            writeBulkString(element);
        }
    }

}
