package team.unnamed.redis.serialize;

import team.unnamed.redis.protocol.Resp;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Responsible for writing information to
 * a {@link OutputStream}
 */
public interface RespWriter {

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
     * Returns a {@link RespWriter} that always
     * writes the given {@code bytes}
     */
    static RespWriter bytes(byte[] bytes) {
        return output -> Resp.writeBulkString(output, bytes);
    }

    /**
     * Returns a {@link RespWriter} that always
     * writes the given {@code string}
     */
    static RespWriter bulkString(String string) {
        return output -> Resp.writeBulkString(output, string.getBytes(Resp.CHARSET));
    }

}