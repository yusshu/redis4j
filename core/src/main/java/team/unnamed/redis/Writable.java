package team.unnamed.redis;

import team.unnamed.redis.datatype.RespStrings;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Responsible for writing information to
 * a {@link OutputStream}
 */
@FunctionalInterface
public interface Writable {

    /**
     * Writes the information into the
     * given {@code output} stream.
     *
     * <strong>Implementations should not close
     * the provided {@link OutputStream}</strong>
     *
     * @throws IOException For any error while
     * writing the data
     */
    void write(OutputStream output) throws IOException;


    /**
     * Returns a {@link Writable} that always
     * writes the given {@code bytes}
     */
    static Writable bytes(byte[] bytes) {
        return output -> RespStrings.writeBulkString(output, bytes);
    }

    /**
     * Returns a {@link Writable} that always
     * writes the given {@code string}
     */
    static Writable bulkString(String string) {
        return output -> RespStrings.writeBulkString(output, string.getBytes(Resp.CHARSET));
    }

}