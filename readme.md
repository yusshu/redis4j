# redis4j
A fast, modular and very lightweight Redis client for the JVM

## Comparison
These are the results of a benchmark testing Jedis and Redis4j
```
Benchmark                                           Mode  Cnt   Score   Error  Units
RedisGetAndSetBenchmark.jedisGetAndSet              avgt    2   2.825          us/op
RedisGetAndSetBenchmark.thisLibraryCachedGetAndSet  avgt    2   0.004          us/op
RedisGetAndSetBenchmark.thisLibraryGetAndSet        avgt    2  29.829          us/op
```
Currently, cached get and set operations are 6 times faster than Jedis operations, however,
uncached operations are 5 times slower (wip)