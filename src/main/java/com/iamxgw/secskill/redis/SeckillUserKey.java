package com.iamxgw.secskill.redis;

public class SeckillUserKey extends BaseKey {
    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;

    private SeckillUserKey(String prefix) {
        super(TOKEN_EXPIRE, prefix);
    }

    public static SeckillUserKey token = new SeckillUserKey("token");
    public static SeckillUserKey getById = new SeckillUserKey("id");
}
