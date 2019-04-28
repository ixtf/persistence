package event;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

/**
 * @author jzb 2019-02-13
 */

public class MyPrinter2 extends JedisPubSub {
    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "192.168.0.38");

    public static void main(String[] args) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            jedis.subscribe(new MyPrinter1(), "SilkBarcodePrinter-2");
        }
    }

    public void onMessage(String channel, String message) {
        final String join = String.join("\t", channel, message);
        System.out.println(join);
    }

    public void onSubscribe(String channel, int subscribedChannels) {
        final String join = String.join("\t", "onSubscribe", channel, "" + subscribedChannels);
        System.out.println(join);
    }

    public void onUnsubscribe(String channel, int subscribedChannels) {
    }

    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    public void onPMessage(String pattern, String channel, String message) {
    }
}
