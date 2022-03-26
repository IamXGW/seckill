package com.iamxgw.secskill.controller;

import com.iamxgw.secskill.access.AccessLimit;
import com.iamxgw.secskill.domain.SeckillOrder;
import com.iamxgw.secskill.domain.SeckillUser;
import com.iamxgw.secskill.rabbitmq.MQSender;
import com.iamxgw.secskill.rabbitmq.SeckillMessage;
import com.iamxgw.secskill.redis.GoodsKey;
import com.iamxgw.secskill.redis.RedisService;
import com.iamxgw.secskill.result.CodeMsg;
import com.iamxgw.secskill.result.Result;
import com.iamxgw.secskill.service.GoodsService;
import com.iamxgw.secskill.service.OrderService;
import com.iamxgw.secskill.service.SeckillService;
import com.iamxgw.secskill.service.SeckillUserService;
import com.iamxgw.secskill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    private Map<Long, Boolean> localOverMap = new HashMap<>();

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();

        if (goodsVoList == null) {
            return;
        }

        for (GoodsVo goodsVo : goodsVoList) {
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }
    }

    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(Model model, SeckillUser user,
                                         @RequestParam("goodsId") long goodsId,
//                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode ) {
                                         @RequestParam("verifyCode") int verifyCode ) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        String path = seckillService.createSeckillPath(user, goodsId);

        return Result.success(path);
    }

    @RequestMapping(value = "/{path}/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model, SeckillUser user,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        boolean check = seckillService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 内存标记，减少对 Redis 的访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 预减库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);

        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 查看用户是否已有秒杀订单
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getPhone(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.SECKILL_REPEAT);
        }

        // 入队
        SeckillMessage message = new SeckillMessage();
        message.setSeckillUser(user);
        message.setSeckillGoodsId(goodsId);

        sender.sendSeckillMessage(message);

        return Result.success(0);
    }

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, SeckillUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result = seckillService.getSeckillResult(user.getPhone(), goodsId);

        return Result.success(result);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletResponse response, SeckillUser user,
                                         @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        try {
            BufferedImage image = seckillService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
