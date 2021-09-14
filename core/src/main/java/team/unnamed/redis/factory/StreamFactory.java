package team.unnamed.redis.factory;

import team.unnamed.redis.io.RespInputStream;
import team.unnamed.redis.io.RespOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Responsible for converting input and output
 * streams to our RESP input and output streams,
 * this also adds more extensibility
 */
public interface StreamFactory {

    /**
     * Decorates the given {@code input} as a
     * {@link RespInputStream}
     * @param input Input stream to decorate
     * @return Decorated input stream
     */
    RespInputStream decorate(InputStream input);

    /**
     * Decorates the given {@code output} as a
     * {@link RespOutputStream}
     * @param output Output stream to decorate
     * @return Decorated output stream
     */
    RespOutputStream decorate(OutputStream output);

}
