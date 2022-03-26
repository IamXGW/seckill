package com.iamxgw.secskill.controller;

import com.iamxgw.secskill.rabbitmq.MQSender;
import com.iamxgw.secskill.redis.OrderKey;
import com.iamxgw.secskill.redis.RedisService;
import com.iamxgw.secskill.redis.SeckillKey;
import com.iamxgw.secskill.result.Result;
import com.iamxgw.secskill.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/thymeleafs")
    public String thymeleaf(Model model) {
        model.addAttribute("name", "IamXGW");
        return "hello";
    }

    @RequestMapping("/reset")
    @ResponseBody
    public Result<Boolean> reset() {
        /*
        SQL
        delete from seckill_order;
        delete from order_info;
        update seckill_goods set stock_count = 10 where goods_id = 1;
        * */

        redisService.delete(OrderKey.getSeckillOrderByUidGid);
        redisService.delete(SeckillKey.isGoodsOver);

        return Result.success(true);
    }
}
