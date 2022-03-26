package com.iamxgw.secskill.rabbitmq;

import com.iamxgw.secskill.domain.SeckillGoods;
import com.iamxgw.secskill.domain.SeckillUser;

public class SeckillMessage {
    private SeckillUser seckillUser;
    private Long seckillGoodsId;

    public SeckillUser getSeckillUser() {
        return seckillUser;
    }

    public void setSeckillUser(SeckillUser seckillUser) {
        this.seckillUser = seckillUser;
    }

    public Long getSeckillGoodsId() {
        return seckillGoodsId;
    }

    public void setSeckillGoodsId(Long seckillGoodsId) {
        this.seckillGoodsId = seckillGoodsId;
    }
}
