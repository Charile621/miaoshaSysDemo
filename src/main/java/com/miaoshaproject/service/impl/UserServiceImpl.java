package com.miaoshaproject.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserOrderDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserOrderDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private UserOrderDOMapper userOrderDOMapper;

    @Autowired
    private OrderService orderService;

    @Override
    public UserModel getUserById(Integer id)
    {
        //调用UserDOMapper获取到对应用户的dataobject
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null)
        {
            return null;
        }
        //通过用户ID获取用户对应的加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel==null)
        {
            throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR);
        }
        ValidationResult validationResult = validator.validate(userModel);
        if(validationResult.isHasErrors())
        {
            throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR,validationResult.getErrMsg());
        }

        //实现model->dataobject方法
        UserDO userDO = convertFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
            userModel.setUserId(userDO.getId());
        }catch(DuplicateKeyException ex)
        {
            throw new BusinessException(EmBusinessError.PARAMETER_VAILDATION_ERROR,"手机号已经被注册");
        }



        UserDO newUser = userDOMapper.selectByTelphone(userDO.getTelphone());
        userModel.setUserId(newUser.getId());
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
        return;
    }

    @Override
    public UserModel vaildateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户的手机号获取用户的信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO==null)
        {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);

        //比对用户信息内的加密的密码是否和传进来的密码相匹配
        if(!StringUtils.equals(encrptPassword,userModel.getEncrptPassword()))
        {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    @Override
    public JSONArray getOrders(Integer userId) throws BusinessException {

        //根据用户id查出属于自己的所有订单的id
        List<UserOrderDO> userOrderDOS = userOrderDOMapper.selectByUserId(userId);
        List<String> orderIds = userOrderDOS.stream().map(userOrderDO -> {
            return userOrderDO.getOrderId();
        }).collect(Collectors.toList());

        //根据订单id去查询具体订单信息并构造成json返回
        return orderService.getOrdersByIds(orderIds);
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel)
    {
        if(userModel == null)
        {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        userPasswordDO.setUserId(userModel.getUserId());
        return userPasswordDO;
    }
    private UserDO convertFromModel(UserModel userModel)
    {
        if(userModel==null)
        {
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }


    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO)
    {
        if(userDO==null)
        {
            return null;
        }

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPasswordDO!=null)
        {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }
}
