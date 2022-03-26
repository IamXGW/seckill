package com.iamxgw.secskill.redis;

public interface KeyPrefix {
    public int expiresSeconds();
    public String getPrefix();
}
