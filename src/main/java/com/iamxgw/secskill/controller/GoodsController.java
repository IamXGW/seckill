package com.iamxgw.secskill.controller;

import com.iamxgw.secskill.domain.SeckillUser;
import com.iamxgw.secskill.redis.GoodsKey;
import com.iamxgw.secskill.redis.RedisService;
import com.iamxgw.secskill.result.Result;
import com.iamxgw.secskill.service.GoodsService;
import com.iamxgw.secskill.service.SeckillUserService;
import com.iamxgw.secskill.vo.GoodsDetailVo;
import com.iamxgw.secskill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    // 页面缓存
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user) {
        model.addAttribute("user", user);

        // 检查是否有页面缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);

        SpringWebContext ctx = new SpringWebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
        // 手动渲染，并存入 Redis 缓存
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }

        return html;
    }

    // URL 缓存
    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user, @PathVariable("goodsId") long goodsId) {
        // 检查是否有页面缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);

        // 手动渲染，并存入 Redis 缓存
        GoodsVo goods = goodsService.getGoodsVoById(goodsId);
        model.addAttribute("goods", goods);

        Long startTime = goods.getStartDate().getTime();
        Long endTime = goods.getEndDate().getTime();
        Long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int reaminSeconds = 0;

        if (now < startTime) {
            seckillStatus = 0;
            reaminSeconds = (int) ((startTime - now) / 1000);
        } else if (now > endTime) {
            seckillStatus = 2;
            reaminSeconds = -1;
        } else {
            seckillStatus = 1;
            reaminSeconds = 0;
        }

        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("reaminSeconds", reaminSeconds);

        SpringWebContext ctx = new SpringWebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }

        return html;
    }

    // 前后端分离
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user, @PathVariable("goodsId") long goodsId) {
        GoodsVo goods = goodsService.getGoodsVoById(goodsId);

        Long startTime = goods.getStartDate().getTime();
        Long endTime = goods.getEndDate().getTime();
        Long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int reaminSeconds = 0;

        if (now < startTime) {
            seckillStatus = 0;
            reaminSeconds = (int) ((startTime - now) / 1000);
        } else if (now > endTime) {
            seckillStatus = 2;
            reaminSeconds = -1;
        } else {
            seckillStatus = 1;
            reaminSeconds = 0;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setUser(user);
        goodsDetailVo.setMiaoshaStatus(seckillStatus);
        goodsDetailVo.setRemainSeconds(reaminSeconds);
        goodsDetailVo.setGoods(goods);

        return Result.success(goodsDetailVo);
    }
}

