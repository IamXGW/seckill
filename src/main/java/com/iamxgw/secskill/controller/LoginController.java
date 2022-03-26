package com.iamxgw.secskill.controller;

import com.iamxgw.secskill.result.Result;
import com.iamxgw.secskill.service.SeckillUserService;
import com.iamxgw.secskill.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {
    @Autowired
    SeckillUserService seckillUserService;

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response,  @Valid LoginVo loginVo) {
        log.info(loginVo.toString());

        String token = seckillUserService.login(response, loginVo);
        return Result.success(token);
    }
}
