package hbv.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RedisConfig {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);      // Maximale Verbindungen
        poolConfig.setMaxIdle(5);        // Maximale Leerlaufverbindungen
        poolConfig.setMinIdle(1);        // Minimale Leerlaufverbindungen
        jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
    }

    public static Jedis getConnection() {
        return jedisPool.getResource();  // Kein `auth()` erforderlich
    }
}

