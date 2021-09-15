# redis4j
A fast, modular and very lightweight Redis client for the JVM

## Comparison
These are the results of a benchmark testing Jedis and Redis4j
```
Benchmark                                       Mode  Cnt   Score   Error  Units
RedisGetAndSetBenchmark.jedisGetAndSet          avgt       11.635          us/op
RedisGetAndSetBenchmark.redis4jCachedGetAndSet  avgt        0.004          us/op
RedisGetAndSetBenchmark.redis4jGetAndSet        avgt       26.467          us/op
```