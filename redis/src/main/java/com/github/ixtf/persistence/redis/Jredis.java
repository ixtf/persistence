package com.github.ixtf.persistence.redis;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jzb 2019-06-24
 */
public abstract class Jredis {
    private static final LoadingCache<Class<? extends JredisOptions>, Jredis> CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public Jredis load(Class<? extends JredisOptions> clazz) throws Exception {
                    final JredisOptions options = clazz.getDeclaredConstructor().newInstance();
                    final JedisPool jedisPool = options.jedisPool();
                    return new Jredis() {
                        @Override
                        public <T> T call(Function<Jedis, T> f) {
                            try (final Jedis jedis = jedisPool.getResource()) {
                                return f.apply(jedis);
                            }
                        }

                        @Override
                        public void run(Consumer<Jedis> c) {
                            try (final Jedis jedis = jedisPool.getResource()) {
                                c.accept(jedis);
                            }
                        }
                    };
                }
            });

    @SneakyThrows
    public static Jredis of(Class<? extends JredisOptions> clazz) {
        return CACHE.get(clazz);
    }

    public abstract <T> T call(Function<Jedis, T> f);

    public abstract void run(Consumer<Jedis> c);

}