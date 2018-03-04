package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置spring和junit整合，junit启动时加载springIOC容器
 * spring-test,junit
 * 需要告诉junit spring配置文件
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

        //注入Dao实现类依赖
        @Resource
        private SeckillDao seckillDao;

        @Test
        public void reduceNumber() {
                Date killTime = new Date();
                int updateCount = seckillDao.reduceNumber(1000L, killTime);
                System.out.println("updateCount = " + updateCount);

        }

        @Test
        public void queryById() {
                long id = 1001;
                Seckill seckill = seckillDao.queryById(id);
                System.out.println(seckill.getName());
                System.out.println(seckill);
                /**
                 * 1000元秒杀IPHONE6
                 *  Seckill{seckillId=1000, name='1000元秒杀IPHONE6', number=100, startTime=Sat Mar 03 00:00:00 CST 2018, endTime=Wed Mar 07 00:00:00 CST 2018, createTime=Sun Mar 04 16:23:12 CST 2018}
                 */
        }

        /**
         * Caused by: org.apache.ibatis.binding.BindingException: Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
         * java没有保存形参的行为
         * queryAll(int offset, int limit) -> queryAll(arg0, arg1)
         */
        @Test
        public void queryAll() {
                List<Seckill> seckillList =  seckillDao.queryAll(0, 100);
                for(Seckill seckill : seckillList) {
                        System.out.println(seckill);
                }
                /**
                 Seckill{seckillId=1000, name='1000元秒杀IPHONE6', number=100, startTime=Sat Mar 03 00:00:00 CST 2018, endTime=Wed Mar 07 00:00:00 CST 2018, createTime=Sun Mar 04 16:23:12 CST 2018}
                 Seckill{seckillId=1001, name='500元秒杀ipad2', number=200, startTime=Sat Mar 03 00:00:00 CST 2018, endTime=Wed Mar 07 00:00:00 CST 2018, createTime=Sun Mar 04 16:23:12 CST 2018}
                 Seckill{seckillId=1002, name='2000元秒杀小米6', number=300, startTime=Sat Mar 03 00:00:00 CST 2018, endTime=Wed Mar 07 00:00:00 CST 2018, createTime=Sun Mar 04 16:23:12 CST 2018}
                 */
        }
}