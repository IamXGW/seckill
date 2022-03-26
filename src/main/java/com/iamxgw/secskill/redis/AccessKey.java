package com.iamxgw.secskill.redis;

public class AccessKey extends BaseKey {
    private AccessKey(int expire, String prefix) {
        super(expire, prefix);
    }

    public static AccessKey withExpire(int seconds) {
        return new AccessKey(seconds, "access");
    }
}
