package com.iamxgw.secskill.controller;

import com.iamxgw.secskill.domain.OrderInfo;
import com.iamxgw.secskill.domain.SeckillUser;
import com.iamxgw.secskill.redis.RedisService;
import com.iamxgw.secskill.result.CodeMsg;
import com.iamxgw.secskill.result.Result;
import com.iamxgw.secskill.service.GoodsService;
import com.iamxgw.secskill.service.OrderService;
import com.iamxgw.secskill.service.SeckillUserService;
import com.iamxgw.secskill.vo.GoodsVo;
import com.iamxgw.secskill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    SeckillUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, SeckillUser user, @RequestParam("orderId") long orderId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);

        return Result.success(vo);
    }
}
