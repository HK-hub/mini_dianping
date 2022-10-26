package com.hk.remark.common.error;

import com.hk.remark.common.resp.ResultCode;

/**
 * @author : HK意境
 * @ClassName : BusinessException
 * @date : 2022/10/26 23:41
 * @description : 依赖的第三方服务，组件等出现异常
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class ServiceException extends BaseException {

    public ServiceException(ResultCode resultCode) {
        this(resultCode.message(), resultCode.code());
    }


    public ServiceException(String message, Integer code) {
        this.message = message;
        this.causes = message;
        this.type = ExceptionType.CustomException;
    }

    public ServiceException(String message, Integer code, Exception exception) {
        this.message = message;
        this.causes = message;
        this.exceptionObject = exception;
        this.type = ExceptionType.CustomException;
    }
}
