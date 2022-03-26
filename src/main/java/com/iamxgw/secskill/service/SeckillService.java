package com.iamxgw.secskill.service;

import com.iamxgw.secskill.domain.OrderInfo;
import com.iamxgw.secskill.domain.SeckillOrder;
import com.iamxgw.secskill.domain.SeckillUser;
import com.iamxgw.secskill.redis.RedisService;
import com.iamxgw.secskill.redis.SeckillKey;
import com.iamxgw.secskill.util.MD5Util;
import com.iamxgw.secskill.util.UUIDUtil;
import com.iamxgw.secskill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class SeckillService {
    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        // 减库存
        boolean success = goodsService.reduceStock(goods);

        if (success) {
            // 下订单
            return orderService.createOrder(user, goods);
        } else {
            setGoodsOver(goods.getId());
            return null;
        }
    }

    public long getSeckillResult(Long phone, long goodsId) {
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(phone, goodsId);
        if (order != null) {
            return order.getOrderId();
        } else {
            boolean isOVer = getGoodsOver(goodsId);

            if (isOVer) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(Long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver, "" + goodsId);
    }

    public String createSeckillPath(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }

        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(SeckillKey.seckillPath, "" + user.getPhone() + "_" + goodsId, str);

        return str;
    }

    public boolean checkPath(SeckillUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }

        String redisPath = redisService.get(SeckillKey.seckillPath, "" + user.getPhone() + "_" + goodsId, String.class);

        return path.equals(redisPath);
    }

    public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 22));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);

        redisService.set(SeckillKey.seckillVerifyCode, user.getPhone() + "," + goodsId, rnd);
        //输出图片
        return image;
    }

    private static char[] ops = new char[]{'+', '-', '*'};

    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;

        return exp;
    }

    public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0) {
            return false;
        }

        Integer codeOld = redisService.get(SeckillKey.seckillVerifyCode, user.getPhone() + "," + goodsId, Integer.class);

        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }

        redisService.delete(SeckillKey.seckillVerifyCode, user.getPhone() + "," + goodsId);

        return true;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(exp);
        } catch (Exception e) {
            e.printStackTrace();

            return 0;
        }
    }
}
