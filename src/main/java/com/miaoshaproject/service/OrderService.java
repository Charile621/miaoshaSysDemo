package com.miaoshaproject.service;

import com.alibaba.fastjson2.JSONArray;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

import java.util.List;

public interface OrderService {

    //使用1.通过前端url上穿过来秒杀活动Id,然后下单接口内校验对应id是否属于对应商品且活动已开始
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中则以秒杀活动价格下单
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId,Integer amount) throws BusinessException;

    JSONArray getOrdersByIds(List<String> orderIds) throws BusinessException;
}
