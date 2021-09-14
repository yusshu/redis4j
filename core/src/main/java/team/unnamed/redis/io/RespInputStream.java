package team.unnamed.redis.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Abstraction giving access to methods for
 * reading data following the Redis Serialization
 * Protocol
 * @author yusshu (Andre Roldan)
 */
public abstract class RespInputStream extends FilterInputStream {

    protected RespInputStream(InputStream in) {
        super(in);
    }

    public abstract Object[] readArray() throws IOException;

    public abstract int readInt() throws IOException;

    public abstract String readSimpleString() throws IOException;

    public abstract byte[] readBulkString() throws IOException;

}
