package mes;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author jzb 2019-06-15
 */
public class MesTest {
    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "10.2.0.213");
//    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "10.61.0.14");

    public static void main(String[] args) {
    }
}
