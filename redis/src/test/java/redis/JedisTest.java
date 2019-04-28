package redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import lombok.SneakyThrows;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-13
 */
public class JedisTest {
//    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "10.2.0.213");
    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "192.168.0.38");
//    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "10.61.0.14");

    @SneakyThrows
    public static void main(String[] args) {
        // 5c8b98dd276e570001ebdd6b
        // 5c8b8355276e5700015bf023
        final String redisKey = "SilkCarRuntime[3000F2507]";
//        final Set<String> fields = Sets.newHashSet();
        test("SilkCarRuntime[3000F2507]");
        test("SilkCarRuntime[3000F2756]");
        System.out.println("end");
    }

    private static void test(String redisKey) throws IOException {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            final Map<String, String> map = jedis.hgetAll(redisKey);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                final String field = entry.getKey();
                if (!field.startsWith("EventSource")) {
                    continue;
                }
                final JsonNode jsonNode = MAPPER.readTree(entry.getValue());
                if (!"JikonAdapterSilkDetachEvent".equals(jsonNode.get("type").asText(null))) {
                    continue;
                }
                final String spindleCode = jsonNode.get("command").get("spindleCode").asText(null);
                if (J.isBlank(spindleCode)) {
                    jedis.hdel(redisKey,field);
//                    fields.add(field);
//                    System.out.println(field);
                }
            }
//            jedis.hdel(redisKey, fields.toArray(new String[fields.size()]));
//            final Long l = jedis.publish("SilkBarcodePrinter-test-C5", "testtest");
//            final List<String> strings = jedis.pubsubChannels("SilkBarcodePrinter-*");
//            System.out.println(strings);
//            final Map<String, String> map = jedis.pubsubNumSub("SilkBarcodePrinter-*");
//            System.out.println(map);
//            jedis.hset("test", ImmutableMap.of("key2", "key2-update", "key4", "key4"));
//            System.out.println(jedis);
//
//            jedis.set("ttl-test", "3");
//            final Long incr = jedis.incr("ttl-test");
//            System.out.println(incr);
//            final String s = jedis.get("ttl-test");
//            System.out.println(s);
//            final Long ttl = jedis.ttl("ttl-test");
//            System.out.println(ttl);
        }
    }
}