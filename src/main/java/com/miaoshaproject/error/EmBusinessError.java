package com.miaoshaproject.error;

public enum EmBusinessError implements CommonError{
    //通用错误类型10001
    PARAMETER_VAILDATION_ERROR(10001,"参数不合法"),

    UNKONWN_ERROR(10002,"未知错误"),
    //20000开头为用户信息相关错误信息定义
    USER_NOT_EXIST(20001,"用户不存在")
    ;

    private EmBusinessError(int erroeCode,String errMsg)
    {
        this.erroeCode = erroeCode;
        this.errMsg = errMsg;
    }

    private int erroeCode;
    private String errMsg;

    @Override
    public int getErrorCode() {
        return this.erroeCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
