package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml"})
public class SeckillServiceImplTest {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private SeckillService seckillService;

        @Test
        public void getSeckillList() {
                List<Seckill> list = seckillService.getSeckillList();
                logger.info("list={}", list);
        }

        @Test
        public void getById() {
                long id = 1000;
                Seckill seckill = seckillService.getById(id);
                logger.info("seckill={}", seckill);
        }

        @Test
        public void exportSeckillUrl() {
                long id = 1002;
                Exposer exposer = seckillService.exportSeckillUrl(id);
                logger.info("exposer={}", exposer);
                /**
                 * exposer=Exposer{exposed=true, md5='16f839b070d5a248d3621cb1b9ee3673', seckillId=1000, now=0, start=0, end=0}
                 */
        }

        @Test
        public void executeSeckill() {
                long id = 1002;
                long userPhone = 18888888888L;
                String md5 = "c9cc479dbc73374bff174f173fadd4d3";
                SeckillExecution seckillExecution = seckillService.executeSeckill(id, userPhone, md5);
                logger.info("seckillExecution={}",seckillExecution);
        }

        //业务流程单元测试
        @Test
        public void testSeckillLogic() throws Exception {
                long id = 1001L;
                long userPhone = 18888888888L;
                Exposer exposer = seckillService.exportSeckillUrl(id);
                if(exposer.isExposed()) {
                        logger.info("exposer={}", exposer);
                        try {
                                SeckillExecution seckillExecution = seckillService.executeSeckill(id, userPhone, exposer.getMd5());
                                logger.info("seckillExecution={}", seckillExecution);
                        } catch (RepeatKillException e) {
                                logger.error(e.getMessage());
                        } catch (SeckillCloseException e) {
                                logger.error(e.getMessage());
                        }
                } else {
                        //秒杀未开启
                        logger.warn("exposer={}",exposer);
                }
        }
}