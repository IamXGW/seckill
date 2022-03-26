package com.iamxgw.secskill.redis;

public class OrderKey extends BaseKey {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getSeckillOrderByUidGid = new OrderKey("moug");
}
