package team.unnamed.redis;

import team.unnamed.redis.io.RespWritable;

/**
 * Utility class containing all the
 * existing redis commands
 */
public final class RedisCommands {

    public static final RespWritable
            COPY = command("COPY"),
            DEL = command("DEL"),
            ECHO = command("ECHO"),
            EXISTS = command("EXISTS"),
            EXPIRE = command("EXPIRE"),
            GET = command("GET"),
            KEYS = command("KEYS"),
            PING = command("PING"),
            QUIT = command("QUIT"),
            PUBLISH = command("PUBLISH"),
            SET = command("SET"),
            SUBSCRIBE = command("SUBSCRIBE");

    private RedisCommands() {
    }

    private static RespWritable command(String name) {
        return RespWritable.bulkString(name);
    }

}
