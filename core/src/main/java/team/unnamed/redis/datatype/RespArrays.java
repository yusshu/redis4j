package team.unnamed.redis.datatype;

import team.unnamed.redis.Resp;
import team.unnamed.redis.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class for working with arrays following the
 * RESP (REdis Serialization Protocol) specification
 */
public final class RespArrays {

    private RespArrays() {
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
        output.write(Resp.ARRAY_BYTE);

        // array length write
        RespIntegers.writeIntAsString(output, array.length);
        Resp.writeTermination(output);

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
        output.write(Resp.ARRAY_BYTE);

        // write length (-1)
        output.write(Resp.SCRIPT_BYTE);
        output.write((byte) '1');

        // termination
        Resp.writeTermination(output);
    }

    public static Object[] readArray(InputStream input) throws IOException {
        int length = RespIntegers.readInteger(input);

        // null array
        if (length == -1) {
            return null;
        }

        Object[] value = new Object[length];
        for (int i = 0; i < length; i++) {
            value[i] = Resp.readResponse(input);
        }
        return value;
    }

}
