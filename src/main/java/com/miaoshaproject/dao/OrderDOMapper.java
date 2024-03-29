package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.OrderDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderDOMapper {
    int deleteByPrimaryKey(String id);

    int insert(OrderDO record);

    int insertSelective(OrderDO record);

    OrderDO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OrderDO record);

    int updateByPrimaryKey(OrderDO record);

    List<OrderDO> selectByIds(@Param("ids")List<String> ids);
}