package com.ants.monitor.common.redis;

import com.ants.monitor.common.tools.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import java.util.*;

/**
 * redis封装后的类
 * Created by zxg on 15/7/20.
 */
@Component
@Slf4j
public class RedisClientTemplateImpl implements RedisClientTemplate {

    private static final Integer EXPIRE_TIME = 60 * 60 * 24 * 15; //15天有效期

    @Autowired
    private RedisDataSource redisDataSource;

    public void disconnect() {
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        shardedJedis.disconnect();
    }


    @Override
    public <T> T lazyGet(String key, Class<?> cls) {
        try {

            String jsonString = this.get(key);
            if (null == jsonString) {
                return null;
            }
            return (T) JsonUtil.jsonStrToObject(jsonString, cls);

        } catch (Exception e) {
            log.error("lazyGet error:", e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> lazyGetList(String key, Class<?> cls) {

        try {

            String jsonString = this.get(key);
            if (null == jsonString) {
                return null;
            }
            return JsonUtil.jsonStrToList(jsonString, cls);

        } catch (Exception e) {
            log.error("lazyGetList error:", e);

            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String lazySet(String key, Object value, Integer expire) {

        try {

            if (null == value) {
                return null;
            }
            String json = JsonUtil.objectToJsonStr(value);

            if (expire == null) {
                return this.set(key, json);
            }
            return this.setStringWithTime(key, json, expire);
        } catch (Exception e) {
            log.error("lazySet error:", e);

            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void setNone(String key) {
        setStringWithTime(key, RedisKeyBean.NULL_OBJECT, RedisKeyBean.RREDIS_EXP_HOURS);
    }

    @Override
    public boolean isNone(String redisString) {
        return RedisKeyBean.NULL_OBJECT.equals(redisString);
    }

    /*=============普通String的操作============================*/

    /**
     * 设置单个值
     * <p/>
     * 将字符串值 value 关联到 key 。
     * 如果 key 已经持有其他值， setString 就覆写旧值，无视类型。
     * 对于某个原本带有生存时间（TTL）的键来说， 当 setString 成功在这个键上执行时， 这个键原有的 TTL 将被清除。
     * 时间复杂度：O(1)
     *
     * @param key   key
     * @param value string value
     * @return 在设置操作成功完成时，才返回 OK 。
     */
    @Override
    public String set(final String key, final String value) {
        String result = null;

        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.set(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    /**
     * 将值 value 关联到 key ，并将 key 的生存时间设为 expire (以秒为单位)。
     * 如果 key 已经存在， 将覆写旧值。
     * 类似于以下两个命令:
     * SET key value
     * EXPIRE key expire # 设置生存时间
     * 不同之处是这个方法是一个原子性(atomic)操作，关联值和设置生存时间两个动作会在同一时间内完成，在 Redis 用作缓存时，非常实用。
     * 时间复杂度：O(1)
     *
     * @param key    key
     * @param value  string value
     * @param expire 生命周期,单位（秒）
     * @return 设置成功时返回 OK 。当 expire 参数不合法时，返回一个错误。
     */
    @Override
    public String setStringWithTime(final String key, final String value, Integer expire) {
        String result = null;

        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        if (null == expire) {
            expire = EXPIRE_TIME;
        }

        boolean broken = false;
        try {
            result = shardedJedis.setex(key, expire, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;

    }



    /**
     * 获取单个值
     *
     * @param key
     * @return
     */
    @Override
    public String get(final String key) {
        String result = null;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }

        boolean broken = false;
        try {
            result = shardedJedis.get(key);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }


    //判断key值存在不存在
    @Override
    public Boolean exists(final String key) {
        Boolean result = false;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.exists(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public Long setMapKey(String key, String field, String value) {
        Long result = 0L;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.hset(key, field, value);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public String getMapKey(String key, String field) {
        String result = null;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.hget(key, field);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public Long LpushList(String key, String value) {
        Long result = 0L;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.lpush(key, value);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public Long RpushList(String key, String value) {
        Long result = 0L;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.rpush(key, value);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public String RpopList(String key) {
        String result = null;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.rpop(key);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public Integer listSize(String key) {
        Integer result = null;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.llen(key).intValue();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public List<String> getList(String key, Integer start, Integer end) {
        List<String> result = null;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.lrange(key,start,end);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public Long addSet(String key, String value) {
        Long result = 0L;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.sadd(key, value);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

    @Override
    public Set<String> getSet(String key) {
        Set<String> result = null;
        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
        if (shardedJedis == null) {
            return result;
        }
        boolean broken = false;
        try {
            result = shardedJedis.smembers(key);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            broken = true;
        } finally {
            redisDataSource.returnResource(shardedJedis, broken);
        }
        return result;
    }

}