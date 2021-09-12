package team.unnamed.redis;

import team.unnamed.redis.Writable;

/**
 * Utility class containing all the
 * existing redis commands
 */
public final class RedisCommands {

    public static final Writable
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
            SET = command("SET");

    private RedisCommands() {
    }

    private static Writable command(String name) {
        return Writable.bulkString(name);
    }

}
