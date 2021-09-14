package team.unnamed.redis.factory;

import team.unnamed.redis.io.BufferedRespInputStream;
import team.unnamed.redis.io.BufferedRespOutputStream;
import team.unnamed.redis.io.RespInputStream;
import team.unnamed.redis.io.RespOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of {@link StreamFactory} that wraps
 * the original {@link InputStream} and {@link OutputStream}
 * in {@link BufferedRespInputStream} and {@link BufferedRespOutputStream},
 * respectively, this is the default and recommended implementation
 */
public class BufferedStreamFactory implements StreamFactory {

    private final int bufferLength;

    public BufferedStreamFactory(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    public BufferedStreamFactory() {
        this.bufferLength = 8192;
    }

    @Override
    public RespInputStream decorate(InputStream input) {
        return new BufferedRespInputStream(input, bufferLength);
    }

    @Override
    public RespOutputStream decorate(OutputStream output) {
        return new BufferedRespOutputStream(output, bufferLength);
    }

}
