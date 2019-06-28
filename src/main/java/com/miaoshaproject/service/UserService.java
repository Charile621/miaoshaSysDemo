package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /*
    telphone:用户手机
    encrptPassword:用户加密后的密码
    * */
    UserModel vaildateLogin(String telphone,String encrptPassword) throws BusinessException;
}
