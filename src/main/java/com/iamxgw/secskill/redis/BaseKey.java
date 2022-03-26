package com.iamxgw.secskill.redis;

public abstract class BaseKey implements KeyPrefix{
    private int expiresSeconds;
    private String prefix;

    public BaseKey(String prefix){
        this.prefix = prefix;
    }

    public BaseKey(int expiresSeconds, String prefix){
        this.expiresSeconds = expiresSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expiresSeconds() {
        return expiresSeconds;
    }

    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
