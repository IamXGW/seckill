package com.iamxgw.secskill.access;

import com.iamxgw.secskill.domain.SeckillUser;

public class UserContext {
    public static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<SeckillUser>();

    public static void setUser(SeckillUser user) {
        userHolder.set(user);
    }

    public static SeckillUser getUser() {
        return userHolder.get();
    }
}
