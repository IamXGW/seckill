package com.iamxgw.secskill.rabbitmq;

import com.iamxgw.secskill.domain.SeckillOrder;
import com.iamxgw.secskill.domain.SeckillUser;
import com.iamxgw.secskill.redis.RedisService;
import com.iamxgw.secskill.result.CodeMsg;
import com.iamxgw.secskill.result.Result;
import com.iamxgw.secskill.service.GoodsService;
import com.iamxgw.secskill.service.OrderService;
import com.iamxgw.secskill.service.SeckillService;
import com.iamxgw.secskill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void receive(String message) {
        log.info("receive message : " + message);
        SeckillMessage seckillMessage = RedisService.stringToBean(message, SeckillMessage.class);

        SeckillUser user = seckillMessage.getSeckillUser();
        if (user == null) {
            System.out.println("user null");
        }
        Long goodsId = seckillMessage.getSeckillGoodsId();
        if (goodsId == null) {
            System.out.println("goodis null");
        }

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long stock = goods.getStockCount();
        if (stock < 0) {
            return;
        }

        // 查看用户是否已有秒杀订单
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getPhone(), goodsId);
        if (order != null) {
            return;
        }

        // 生成秒杀订单
        seckillService.seckill(user, goods);
    }
}
