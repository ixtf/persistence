package mes;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jzb 2019-06-15
 */
public class MesTest {
    //    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "10.2.0.213");
    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "10.61.0.14");

    public static void main(String[] args) {
        final String s = "SilkCarRuntime[YJ045P0004L]";
        final Pattern pattern = Pattern.compile("^SilkCarRuntime\\[(\\w+)\\]$");
        final Matcher matcher = pattern.matcher(s);
        final boolean b = matcher.find();
        System.out.println(b);
        final String group = matcher.group(1);
        System.out.println(group);

//        try (final Jedis jedis = JEDIS_POOL.getResource()) {
//            final Set<String> keys = jedis.keys("SilkCarRuntime\\[*");
//            final String key = IterableUtils.get(keys, 0);
//            System.out.println(key);
//            System.out.println(keys.size());
//        }
        System.out.println("end");

//        final RedisClient redisClient = RedisClient.create("redis://10.2.0.213");
//        final StatefulRedisConnection<String, String> connect = redisClient.connect();
//        final RedisReactiveCommands<String, String> reactive = connect.reactive();
//        final ScanArgs scanArgs = new ScanArgs();
//        final KeyScanCursor<String> keyScanCursor = reactive.scan(scanArgs).block();
//        final List<String> keys = keyScanCursor.getKeys();
    }
}
