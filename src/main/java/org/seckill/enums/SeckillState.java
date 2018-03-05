package org.seckill.enums;

//使用枚举表述常量字段
public enum SeckillState {
        SUCCESS(1, "秒杀成功"),
        END(0, "秒杀结束"),
        REPEAE_KILL(-1, "重复秒杀"),
        INNER_ERROR(-2, "系统异常"),
        DATA_REWRITE(-3, "数据篡改");

        private int state;
        private String stateInfo;

        SeckillState(int state, String stateInfo) {
                this.state = state;
                this.stateInfo = stateInfo;
        }

        public int getState() {
                return state;
        }

        public String getStateInfo() {
                return stateInfo;
        }

        public static SeckillState stateOf(int index) {
                for(SeckillState state : values()) {
                        if(state.getState() == index) {
                                return state;
                        }
                }
                return null;
        }
}
