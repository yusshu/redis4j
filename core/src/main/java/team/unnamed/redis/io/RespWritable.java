package team.unnamed.redis.io;

import team.unnamed.redis.Resp;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Responsible for writing information to
 * a {@link OutputStream}
 */
@FunctionalInterface
public interface RespWritable {

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
    void write(RespOutputStream output) throws IOException;


    /**
     * Returns a {@link RespWritable} that always
     * writes the given {@code bytes}
     */
    static RespWritable bytes(byte[] bytes) {
        return output -> output.writeBulkString(bytes);
    }

    /**
     * Returns a {@link RespWritable} that always
     * writes the given {@code string}
     */
    static RespWritable bulkString(String string) {
        return output -> output.writeBulkString(string.getBytes(Resp.CHARSET));
    }

}