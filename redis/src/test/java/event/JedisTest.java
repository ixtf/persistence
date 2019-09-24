package event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        try (final Jedis jedis = JEDIS_POOL.getResource()) {
            final Map<String, String> map = jedis.hgetAll("SilkCarRuntime[3000P30292]_todo");
            final Map<String, String> newMap = map.keySet().stream().filter(key -> {
                try {
                    if (!key.startsWith("EventSource.")) {
                        return true;
                    }
                    final String json = map.get(key);
                    final JsonNode jsonNode = MAPPER.readTree(json);
                    if (Objects.equals("if_riamb", jsonNode.get("operator").get("id").asText())) {
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toMap(Function.identity(), map::get));
            jedis.hset("SilkCarRuntime[3000P30292]", newMap);
            System.out.println("end");
        }
    }
}