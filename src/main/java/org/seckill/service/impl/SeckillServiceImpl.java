package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillState;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("seckillService")
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired      //@Resource
    private SeckillDao seckillDao;
    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //盐值字符串用来混淆md5
    private final String salt = "sakjdjkzxkc2asda45$&%*$&@21#!*@51Jj#@HHNFxz";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * 使用redis作为缓存减少mysql服务器压力
     * @param seckillId
     * @return
     */
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存优化,建立在超时的基础上维护一致性
        /**
         * get from cache
         * if none
         *  get db
         *  put cache
         */
        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null) {
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //转化特定字符串的过程,不可逆
        String md5 = getMd5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMd5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1：开发团队达成一致约定，明确标注事务方法的编程风格
     * 2：保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC、HTTP请求或者剥离到事务方法外面
     * 3：不是所有的方法都需要事务如只有一条修改操作，只读操作不需要事务控制
     */

    /**
     * 第一次优化
     * 先insert再update，减少一个事务中rowLock的持有时间，增大并发量
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        if (md5 == null
                || !md5.equals(getMd5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime = new Date();
        //减库存
        try {
            //减库存成功,记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新到记录意味秒杀结束, rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //秒杀成功, commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillState.SUCCESS, successKilled);
                }
            }

//                //减库存成功,记录购买行为
//                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
//                if (insertCount <= 0) {
//                    //重复秒杀
//                    throw new RepeatKillException("seckill repeated");
//                } else {
//                    //秒杀成功
//                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
//                    return new SeckillExecution(seckillId, SeckillState.SUCCESS, successKilled);
//                }
        } catch (SeckillCloseException e) {
            throw e;
        } catch (RepeatKillException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译器异常转化为运行期异常，只有运行期异常才会使事务rollback
            throw new SeckillException("seckill inner error : " + e.getMessage());
        }
    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        if(md5 == null || !md5.equals(getMd5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillState.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        //执行存储过程
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if(result == 1) {
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillState.SUCCESS,sk);
            } else {
                return new SeckillExecution(seckillId, SeckillState.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillState.INNER_ERROR);
        }
    }
}
