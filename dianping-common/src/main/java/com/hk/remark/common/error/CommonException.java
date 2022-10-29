package com.hk.remark.common.error;

import com.hk.remark.common.resp.ResultCode;

/**
 * @author : HK意境
 * @ClassName : BusinessException
 * @date : 2022/10/26 23:41
 * @description : 普通异常，一些公共依赖，工具异常
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class CustomException extends BaseException {

    public CustomException(ResultCode resultCode) {
        this(resultCode.message(), resultCode.code());
    }


    public CustomException(String message, Integer code) {
        this.message = message;
        this.causes = message;
        this.type = ExceptionType.CustomException;
    }

    public CustomException(String message, Integer code, Exception exception) {
        this.message = message;
        this.causes = message;
        this.exceptionObject = exception;
        this.type = ExceptionType.CustomException;
    }
}
