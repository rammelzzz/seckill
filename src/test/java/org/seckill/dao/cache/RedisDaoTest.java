package org.seckill.dao.cache;

import ch.qos.logback.core.net.SyslogOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @Author: rammelzzz
 * @Description:
 * @Date: Created in 下午7:52 18-4-16
 * @Modified By:
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {

    private long id = 1003;
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private RedisDao redisDao;

    @Test
    public void testSeckill() {
        //get and put
        Seckill seckill = redisDao.getSeckill(id);
        if(seckill == null) {
            seckill = seckillDao.queryById(id);
            if(seckill != null) {
                String result = redisDao.putSeckill(seckill);
                System.out.println(result);
                seckill = redisDao.getSeckill(id);
                System.out.println(seckill);
            }
        }
    }


}