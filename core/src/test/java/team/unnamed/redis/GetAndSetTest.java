package team.unnamed.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GetAndSetTest extends LocalRedisTest {

    @Test
    public void test() {
        Assertions.assertEquals("OK", client.set("yusshu", "so smart"));
        Assertions.assertEquals("so smart", client.get("yusshu"));
    }

}
