package xyz.ludwicz.library.redis;

import redis.clients.jedis.Jedis;

public interface RedisCommand<T> {
    T execute(Jedis var1);
}

