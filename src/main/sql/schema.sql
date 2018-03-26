-- 数据库初始化脚本
CREATE DATABASE seckill;
-- 使用数据库
use seckill;
-- 创建秒杀库存表
CREATE TABLE seckill(
  `seckill_id` bigint NOT NULL AUTO_INCREMENT  COMMENT '商品库存id',
  `name` varchar(120) NOT NULL COMMENT '商品的名称',
  `number` int NOT NULL COMMENT '库存数量',
  `start_time` timestamp NOT NULL COMMENT '秒杀开启的时间',
  `end_time` timestamp NOT NULL COMMENT '秒杀结束的时间',
  `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (seckill_id),
  key idx_start_time(start_time),
  key idx_end_time(end_time),
  key idx_create_time(create_time)
)engine = InnoDB AUTO_INCREMENT =1000 DEFAULT CHARSET = UTF8 COMMENT = '秒杀库存表';

-- 初始化数据
insert into seckill(name, number,start_time, end_time)
values
  ('1000元秒杀IPHONE6', 100, '2018-3-26 00:00:00', '2018-3-29 00:00:00'),
  ('500元秒杀ipad2', 200, '2018-3-25 00:00:00', '2018-3-28 00:00:00'),
  ('2000元秒杀小米6', 300, '2018-3-28 00:00:00', '2018-3-31 00:00:00'),
   ('3000元秒杀iphoneX', 200, '2018-3-27 00:00:00', '2018-3-30 00:00:00');

-- 秒杀成功明细表
-- 用户登录认证相关信息
CREATE TABLE success_killed(
  `seckill_id` bigint NOT NULL COMMENT '秒杀商品id',
  `user_phone` bigint NOT NULL COMMENT '用户手机号',
  `state` tinyint NOT NULL DEFAULT -1 COMMENT '状态标识：-1 : 无效 0：成功 1：已付款 2：发货',
  `create_time` TIMESTAMP  NOT NULL COMMENT '创建时间',
  PRIMARY KEY (seckill_id, user_phone) ,/*联合主键*/
  KEY idx_create_time (create_time)
)engine = InnoDB  DEFAULT CHARSET = UTF8 COMMENT = '秒杀成功明细表';

-- 连接数据库控制台
