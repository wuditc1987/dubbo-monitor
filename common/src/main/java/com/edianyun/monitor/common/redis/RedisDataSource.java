package com.edianyun.monitor.common.redis;

import redis.clients.jedis.ShardedJedis;

/**
 * redis最底层，从pool中获得shardedJsdis对象
 * Created by zxg on 15/7/20.
 */
public interface RedisDataSource {

//    从jedis池中获得一个jedis对象
    ShardedJedis getRedisClient();

    //出现异常，将资源返还给pool
    void returnResource(ShardedJedis shardedJedis, boolean broken);
}
