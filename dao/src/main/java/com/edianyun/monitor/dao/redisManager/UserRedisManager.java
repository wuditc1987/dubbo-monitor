package com.edianyun.monitor.dao.redisManager;

import com.edianyun.monitor.common.redis.RedisClientTemplate;
import com.edianyun.monitor.common.redis.RedisKeyBean;
import com.edianyun.monitor.common.tools.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by zxg on 15/12/14.
 * 12:17
 */
@Service
public class UserRedisManager {


    @Autowired
    private RedisClientTemplate redisClientTemplate;

    public void saveUserIpName(String ip,String name){
        String mapKey = String.format(RedisKeyBean.userIpNameKey, TimeUtil.getDateString(new Date()));
        String ipNameField = String.format(RedisKeyBean.userIpNameFieldKey,ip);

        redisClientTemplate.setMapKey(mapKey,ipNameField,name);
    }


    public String getUserIpName(String ip){
        String mapKey = String.format(RedisKeyBean.userIpNameKey, TimeUtil.getDateString(new Date()));
        String ipNameField = String.format(RedisKeyBean.userIpNameFieldKey,ip);

        return redisClientTemplate.getMapKey(mapKey, ipNameField);
    }

}
