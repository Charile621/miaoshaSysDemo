package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";

    //定义exceptionhandler解决未被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex)
    {
        Map<String,Object> data = new HashMap<>();
        if(ex instanceof BusinessException)
        {
            BusinessException businessException = (BusinessException)ex;
            data.put("errorCode",businessException.getErrorCode());
            data.put("errMsg",businessException.getErrMsg());
        }
        else
        {
            ex.printStackTrace();
            data.put("errorCode", EmBusinessError.UNKONWN_ERROR.getErrorCode());
            data.put("errMsg",EmBusinessError.UNKONWN_ERROR.getErrMsg());
        }

        return CommonReturnType.create(data,"fail");
    }
}
