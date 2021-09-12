package team.unnamed.redis.protocol;

import team.unnamed.redis.serialize.RespWriter;

/**
 * Utility class containing all the
 * existing redis commands
 */
public final class RedisCommands {

    public static final RespWriter
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

    private static RespWriter command(String name) {
        return RespWriter.bulkString(name);
    }

}
