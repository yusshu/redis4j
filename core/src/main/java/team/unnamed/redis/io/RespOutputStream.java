package team.unnamed.redis.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstraction giving access to methods for
 * writing data following the Redis Serialization
 * Protocol
 * @author yusshu (Andre Roldan)
 */
public abstract class RespOutputStream extends FilterOutputStream {

    protected RespOutputStream(OutputStream out) {
        super(out);
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
    public abstract void writeInt(int value) throws IOException;

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
    public abstract void writeSimpleString(String value) throws IOException;

    /**
     * Writes the given string {@code value} into this output
     * stream following the RESP specification
     * i.e.
     *   $3\r\nabc\r\n
     *   $11\r\nHello World\r\n
     *   $0\r\n\r\n
     * @throws IOException If write fails
     */
    public abstract void writeBulkString(byte[] value) throws IOException;

    /**
     * Variation of {@link RespOutputStream#writeBulkString}, it just
     * doesn't write the string data termination and the length is '-1',
     * note that an empty bulk string and a null bulk string are totally
     * different.
     * It writes: $-1\r\n
     * @throws IOException If write fails
     */
    public abstract void writeNullBulkString() throws IOException;

    /**
     * Writes the given {@code array} into this output stream following
     * the RESP specification.
     * i.e.
     *   *1\r\n:10\r\n
     *   *0\r\n
     *   *2\r\n+Hello\r\n+World\r\n
     * @throws IOException If write fails
     */
    public abstract void writeArray(RespWritable... array) throws IOException;

    /**
     * Writes the given {@code array} into this output stream following
     * the RESP specification.
     * i.e.
     *   *1\r\n:10\r\n
     *   *0\r\n
     *   *2\r\n+Hello\r\n+World\r\n
     * @throws IOException If write fails
     */
    public abstract void writeArray(byte[]... array) throws IOException;

    /**
     * Writes a null array into this output stream following the RESP
     * specification.
     * It writes:
     *      *-1\r\n
     * Note that an empty array and a null array are totally
     * different
     * @throws IOException If write fails
     */
    public abstract void writeNullArray() throws IOException;

}
