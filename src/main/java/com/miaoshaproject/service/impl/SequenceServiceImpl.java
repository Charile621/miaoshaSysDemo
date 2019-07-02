package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service
public class SequenceServiceImpl implements SequenceService {

    @Autowired
    private SequenceDOMapper sequenceDOMapper;
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo()
    {
        StringBuilder stringBuilder = new StringBuilder();

        //订单号有16位，前8为时间信息年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);

        //中间6位自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i=0;i<6-sequenceStr.length();i++)
        {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //最后两位为分库分表位,暂时写死
        stringBuilder.append("00");
        return stringBuilder.toString();
    }
}
