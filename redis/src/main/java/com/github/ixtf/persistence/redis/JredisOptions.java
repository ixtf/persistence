package com.github.ixtf.persistence.redis;

import redis.clients.jedis.JedisPool;

/**
 * @author jzb 2019-02-18
 */
public abstract class JredisOptions {
    protected abstract JedisPool jedisPool();
}
