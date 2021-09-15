package team.unnamed.redis.pubsub;

/**
 * Represents a subscriber for a set of redis
 * channels, a listener for MESSAGE, PMESSAGE,
 * and other commands sent by the redis-server
 * to the client
 */
public interface RedisSubscriber {

    /**
     * Called when 'MESSAGE' is received, so there was a
     * message publication somewhere, and we received it
     * @param channel The message channel
     * @param message The received message
     */
    default void onMessage(String channel, String message) {
    }

    /**
     * Called when 'PMESSAGE' is received, so there was a
     * message publication somewhere, and we received it
     * @param pattern The channel pattern
     * @param channel The actual channel name
     * @param message The received message
     */
    default void onPMessage(String pattern, String channel, String message) {
    }

    /**
     * Called when 'SUBSCRIBE' is received, so the client
     * successfully subscribed to a channel
     * @param channel The channel name
     * @param subscriptions Count of active subscriptions
     */
    default void onSubscribe(String channel, int subscriptions) {
    }

    /**
     * Called when 'SUBSCRIBE' is received, so the client
     * successfully subscribed to a channel pattern
     * @param pattern The channel name pattern
     * @param subscriptions Count of active subscriptions
     */
    default void onPSubscribe(String pattern, int subscriptions) {
    }

    /**
     * Called when 'UNSUBSCRIBE' is received, so the client
     * successfully unsubscribed to a channel
     * @param channel The channel name
     * @param subscriptions Count of active subscriptions
     */
    default void onUnsubscribe(String channel, int subscriptions) {
    }

    /**
     * Called when 'PUNSUBSCRIBE' is received, so the client
     * successfully unsubscribed to a channel name pattern
     * @param pattern The channel name pattern
     * @param subscriptions Count of active subscriptions
     */
    default void onPUnsubscribe(String pattern, int subscriptions) {
    }

    default void onPong(String message) {
    }

}
