package mes;

import com.github.ixtf.japp.core.J;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;

import static redis.clients.jedis.ScanParams.SCAN_POINTER_START;

/**
 * @author jzb 2019-06-15
 */
public class MesTest {
    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "10.2.0.213");

    public static void main(String[] args) {
        try (final Jedis jedis = JEDIS_POOL.getResource()) {
            String cur = SCAN_POINTER_START;
            do {
                final ScanParams scanParams = new ScanParams().count(100).match("Dyeing-*");
                final ScanResult<String> scanResult = jedis.scan(cur, scanParams);
                for (String key : scanResult.getResult()) {
                    final Map<String, String> map = jedis.hgetAll(key);
                    if (J.isBlank(map.get("id"))) {
                        System.out.println(key);
                    }
                    if (J.isBlank(map.get("dyeingPrepare"))) {
                        System.out.println(key);
                    }
                }
                cur = scanResult.getCursor();
            } while (!SCAN_POINTER_START.equals(cur));
        }
        System.out.println("end");

        final RedisClient redisClient = RedisClient.create("redis://10.2.0.213");
        final StatefulRedisConnection<String, String> connect = redisClient.connect();
        final RedisReactiveCommands<String, String> reactive = connect.reactive();
        final ScanArgs scanArgs = new ScanArgs();
        final KeyScanCursor<String> keyScanCursor = reactive.scan(scanArgs).block();
        final List<String> keys = keyScanCursor.getKeys();
    }
}
