package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.UserOrderDO;

import java.util.List;

public interface UserOrderDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserOrderDO record);

    int insertSelective(UserOrderDO record);

    UserOrderDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserOrderDO record);

    int updateByPrimaryKey(UserOrderDO record);

    List<UserOrderDO> selectByUserId(Integer userId);
}