package team.unnamed.redis;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RedisGetAndSetBenchmark {

    private Jedis jedis;
    private RedisClient redisClient;

    private RedisOperation getOperation;
    private RedisOperation setOperation;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        jedis = new Jedis("127.0.0.1", 6379);
        redisClient = RedisClient.create(new InetSocketAddress("127.0.0.1", 6379));

        jedis.set("test", "test");
        redisClient.set("test", "test");

        getOperation = RedisOperation.get("do");
        setOperation = RedisOperation.set("do", "a flip");
    }

    @Benchmark
    public void thisLibraryCachedGetAndSet() {
        redisClient.execute(setOperation);
        redisClient.execute(getOperation);
    }

    @Benchmark
    public void thisLibraryGetAndSet() {
        redisClient.set("do", "a flip");
        redisClient.get("do");
    }

    @Benchmark
    public void jedisGetAndSet() {
        jedis.set("do", "a flip");
        jedis.get("do");
    }

}
