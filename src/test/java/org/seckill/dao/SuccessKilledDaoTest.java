package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

        @Resource
        private SuccessKilledDao successKilledDao;

        @Test
        public void insertSuccessKilled() {
                /**
                 * 第一次执行：insertCount = 1
                 * 第二次执行：insertCount = 0 联合主键决定
                 */
                int insertCount = successKilledDao.insertSuccessKilled(1001L, 18888888888L);
                System.out.println("insertCount = " + insertCount);
        }

        @Test
        public void queryByIdWithSeckill() {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(1001L, 18888888888L);
                System.out.println(successKilled);
                System.out.println(successKilled.getSeckill());
                /**
                 * 18:32:41.918 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@4f32a3ad] will not be managed by Spring
                 18:32:41.937 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==>  Preparing: select sk.seckill_id, sk.user_phone, sk.create_time, sk.state, s.seckill_id "seckill.seckill_id", s.name "seckill.name", s.number "seckill.number", s.start_time "seckill.start_time", s.end_time "seckill.end_time", s.create_time "seckill.create_time" from success_killed sk inner join seckill s on sk.seckill_id = s.seckill_id where sk.seckill_id = ? and sk.user_phone = ?
                 18:32:42.069 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==> Parameters: 1000(Long), 18888888888(Long)
                 18:32:42.281 [main] DEBUG o.s.d.S.queryByIdWithSeckill - <==      Total: 1
                 18:32:42.286 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@2cb4893b]
                 SuccessKilled{seckillId=1000, userPhone=18888888888, state=-1, createTime=Sun Mar 04 18:28:53 CST 2018}
                 Seckill{seckillId=1000, name='1000元秒杀IPHONE6', number=99, startTime=Sat Mar 03 00:00:00 CST 2018, endTime=Wed Mar 07 00:00:00 CST 2018, createTime=Sun Mar 04 16:23:12 CST 2018}
                 */
        }
}