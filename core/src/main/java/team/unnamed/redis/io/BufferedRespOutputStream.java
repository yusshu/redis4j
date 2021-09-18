package team.unnamed.redis.io;

import team.unnamed.redis.Resp;

import java.io.IOException;
import java.io.OutputStream;

public class BufferedRespOutputStream extends RespOutputStream {

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

    @Override
    public void write(byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
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
        writeNegativeOneAndTermination();
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
        write(Resp.ARRAY_BYTE);
        writeNegativeOneAndTermination();
    }

    @Override
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

    @Override
    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }

}
