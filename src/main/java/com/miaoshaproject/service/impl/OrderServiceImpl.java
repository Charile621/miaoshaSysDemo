package com.miaoshaproject.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.UserOrderDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.UserOrderDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.SequenceService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private UserOrderDOMapper userOrderDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {

        //1.校验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR,"商品信息不存在");
        }

        UserModel userModel = userService.getUserById(userId);
        if(userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR,"用户信息不存在");
        }

        if(amount<=0||amount>99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR,"数量信息不正确");
        }

        //校验活动信息
        if(promoId!=null) {
            //1.校验对应活动是否对应该商品
            if(promoId.intValue()!=itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR,"活动信息不正确");

            }
            else if(itemModel.getPromoModel().getStatus()!=2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR,"活动还未开始");
            }
        }
        //2.落单减库存
        boolean result = itemService.decreaseStock(itemId,amount);
        if(!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH,"库存不足");
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        orderModel.setPromoId(promoId);
        if(promoId!=null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }
        else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));


        //生成交易订单号
        String orderSeq = sequenceService.generateOrderNo();
        orderModel.setId(orderSeq);
        OrderDO orderDO = this.convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //订单信息入用户订单表
        UserOrderDO userOrderDO  = createUserOrderDO(userId, orderSeq);
        userOrderDOMapper.insertSelective(userOrderDO);

        //加上商品的销量
        itemService.increaseSales(itemId,amount);
        //4.返回前端
        return orderModel;
    }

    @Override
    public JSONArray getOrdersByIds(List<String> orderIds) throws BusinessException {
        List<OrderDO> orderDOS = orderDOMapper.selectByIds(orderIds);
        JSONArray orders = new JSONArray();
        orderDOS.stream().forEach(orderDO -> {
            JSONObject order = (JSONObject) JSON.toJSON(orderDO);
            orders.add(order);
        });
        return orders;
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if(orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }

    private UserOrderDO createUserOrderDO (Integer userId, String orderSeq) {
        UserOrderDO userOrderDO = new UserOrderDO();
        userOrderDO.setUserId(userId);
        userOrderDO.setOrderId(orderSeq);
        return userOrderDO;
    }

}
