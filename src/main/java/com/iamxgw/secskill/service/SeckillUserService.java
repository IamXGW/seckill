package com.iamxgw.secskill.service;

import com.iamxgw.secskill.Exception.GlobalException;
import com.iamxgw.secskill.dao.SeckillUserDao;
import com.iamxgw.secskill.domain.SeckillUser;
import com.iamxgw.secskill.redis.RedisService;
import com.iamxgw.secskill.redis.SeckillUserKey;
import com.iamxgw.secskill.result.CodeMsg;
import com.iamxgw.secskill.util.MD5Util;
import com.iamxgw.secskill.util.UUIDUtil;
import com.iamxgw.secskill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    SeckillUserDao seckillUserDao;
    @Autowired
    RedisService redisService;

    public SeckillUser getById(long id) {
        SeckillUser user = redisService.get(SeckillUserKey.getById, "" + id, SeckillUser.class);
        if (user != null) {
            return user;
        }

        user = seckillUserDao.getById(id);
        if (user != null) {
            redisService.set(SeckillUserKey.getById, "" + id, user);
        }

        return user;
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        // 验证手机号
        String mobile = loginVo.getMobile();
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 验证密码
        String formPass = loginVo.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDbPass(formPass, saltDB);
        String dbPass = user.getPassword();

        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        String token = UUIDUtil.uuid();

        addCookie(response, user, token);

        return token;
    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        SeckillUser user = redisService.get(SeckillUserKey.token, token, SeckillUser.class);

        if (user != null) {
            addCookie(response, user, token);
        }

        return user;
    }

    private void addCookie(HttpServletResponse response, SeckillUser user, String token) {
        redisService.set(SeckillUserKey.token, token, user);

        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(SeckillUserKey.TOKEN_EXPIRE);
        cookie.setPath("/");

        response.addCookie(cookie);
    }
}
