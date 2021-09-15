package team.unnamed.redis.pubsub;

import team.unnamed.redis.RedisClientImpl;
import team.unnamed.redis.RedisException;
import team.unnamed.redis.util.Strings;

public class BlockingRedisSubscription implements Runnable {

    private final RedisClientImpl client;
    private final RedisSubscriber subscriber;

    private int subscriptions;

    public BlockingRedisSubscription(
            RedisClientImpl client,
            RedisSubscriber subscriber
    ) {
        this.client = client;
        this.subscriber = subscriber;
    }

    @Override
    public void run() {
        /*
         TODO: This will fail (not in the good way) in case
          of new redis replies because we only check the firsts
          chars
          */
        do {
            Object[] response = client.readArrayResponse();
            byte[] type = (byte[]) response[0];

            switch (type[0]) {
                // 'subscribe'
                case 's': {
                    byte[] channel = (byte[]) response[1];
                    subscriber.onSubscribe(
                            Strings.decode(channel),
                            subscriptions = (int) response[2]
                    );
                    break;
                }

                // 'message'
                case 'm': {
                    byte[] channel = (byte[]) response[1];
                    byte[] message = (byte[]) response[2];
                    subscriber.onMessage(Strings.decode(channel), Strings.decode(message));
                    break;
                }

                // 'unsubscribe'
                case 'u': {
                    byte[] channel = (byte[]) response[1];
                    subscriber.onUnsubscribe(
                            Strings.decode(channel),
                            subscriptions = (int) response[2]
                    );
                    break;
                }

                // pmessage, psubscribe, punsubscribe, pong, we should check the next byte
                case 'p': {
                    byte next = type[1];
                    switch (next) {
                        // 'pmessage'
                        case 'm': {
                            byte[] pattern = (byte[]) response[1];
                            byte[] channel = (byte[]) response[2];
                            byte[] message = (byte[]) response[3];
                            subscriber.onPMessage(
                                    Strings.decode(pattern),
                                    Strings.decode(channel),
                                    Strings.decode(message)
                            );
                            break;
                        }

                        // 'psubscribe'
                        case 's': {
                            byte[] pattern = (byte[]) response[1];
                            subscriber.onPSubscribe(
                                    Strings.decode(pattern),
                                    subscriptions = (int) response[2]
                            );
                            break;
                        }

                        // 'punsubscribe'
                        case 'u': {
                            byte[] pattern = (byte[]) response[1];
                            subscriber.onPUnsubscribe(
                                    Strings.decode(pattern),
                                    subscriptions = (int) response[2]
                            );
                            break;
                        }

                        // 'pong'
                        case 'p': {
                            byte[] pattern = (byte[]) response[1];
                            subscriber.onPong(Strings.decode(pattern));
                            break;
                        }
                    }
                    break;
                }

                default: {
                    throw new RedisException("Unknown message type: " + Strings.decode(type));
                }
            }
        } while (subscriptions != 0);
    }

}
