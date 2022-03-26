package com.iamxgw.secskill.redis;

public class SeckillKey extends BaseKey {
    private SeckillKey(int expiresSeconds, String prefix) {
        super(expiresSeconds, prefix);
    }

    public static SeckillKey isGoodsOver = new SeckillKey(0, "go");
    public static SeckillKey seckillPath = new SeckillKey(60, "sp");
    public static SeckillKey seckillVerifyCode = new SeckillKey(300, "vc");
}
