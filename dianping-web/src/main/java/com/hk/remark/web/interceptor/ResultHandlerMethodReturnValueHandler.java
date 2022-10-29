package com.hk.remark.web.interceptor;

import cn.hutool.core.lang.UUID;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.error.BaseException;
import com.hk.remark.common.error.BusinessException;
import com.hk.remark.common.error.CommonException;
import com.hk.remark.common.error.ExceptionType;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.resp.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author : HK意境
 * @ClassName : ResultHandlerMethodReturnValueHandler
 * @date : 2022/10/27 10:48
 * @description : 统一响应结果格式化，controller 方法返回值二次处理
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class ResultHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    private HandlerMethodReturnValueHandler returnValueHandler ;

    public ResultHandlerMethodReturnValueHandler(HandlerMethodReturnValueHandler returnValueHandler) {
        this.returnValueHandler = returnValueHandler;
    }

    @Override
    public boolean supportsReturnType(MethodParameter methodParameter) {

        return this.returnValueHandler.supportsReturnType(methodParameter);
    }


    /**
     * @methodName : handleReturnValue
     * @author : HK意境
     * @date : 2022/10/27 10:51
     * @description : 处理 handler 方法返回值结果
     * @Todo :
     * @apiNote : 统一封装为 ResponseResult 或者 ExceptionResult
     * @params :
         * @param result 响应结果
         * @param methodParameter 参数
         * @param modelAndViewContainer mvc容器
         * @param nativeWebRequest request请求
     * @return null
     * @throws:
     * @Bug :
     * @Modified :
     * @Version : 1.0.0
     */
    @Override
    public void handleReturnValue(Object result, MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest) throws Exception {

        // 获取 traceId
        String tranceId = nativeWebRequest.getHeader(ReqRespConstants.TRACE_ID);
        if (StringUtils.isEmpty(tranceId)) {
            tranceId = UUID.fastUUID().toString();
        }

        // 日志处理

        // 判断响应结果类型
        if (result instanceof ResponseResult) {
            // 统一响应体类型 或者 自定义异常类型, 已经格式化了
            ResponseResult finalResult = (ResponseResult) result;
            // 设置tranceId
            finalResult.setTraceId(tranceId);

            this.returnValueHandler.handleReturnValue(finalResult, methodParameter, modelAndViewContainer, nativeWebRequest);

        } else if (result instanceof BaseException) {
            BaseException exceptionResult = (BaseException) result;
            exceptionResult.setTraceId(tranceId);
            this.returnValueHandler.handleReturnValue(exceptionResult, methodParameter, modelAndViewContainer, nativeWebRequest);

        } else if (result instanceof Exception) {
            // 异常类型: 非自定义异常，程序运行出现未知异常或产生未预知的异常
            BaseException exceptionResult;
            if (result instanceof RuntimeException) {
                RuntimeException runtimeException = (RuntimeException) result;
                exceptionResult = new CommonException(ResultCode.SERVER_BUSY);
                exceptionResult.setExceptionObject(runtimeException);
                exceptionResult.setMessage(runtimeException.getMessage());
                exceptionResult.setCauses(runtimeException.getCause().toString());
                exceptionResult.setTraceId(tranceId);
                this.returnValueHandler.handleReturnValue(exceptionResult, methodParameter, modelAndViewContainer, nativeWebRequest);
            } else {
                exceptionResult = new BusinessException(ResultCode.SERVER_BUSY);
                exceptionResult.setType(ExceptionType.valueOf(result.getClass().getName()));
                this.returnValueHandler.handleReturnValue(exceptionResult, methodParameter, modelAndViewContainer, nativeWebRequest);
            }

        } else {
            // 其他响应类型: 非异常，都是正确结果
            ResponseResult successResult = new ResponseResult(ResultCode.SUCCESS, result);
            successResult.setTraceId(tranceId);
            this.returnValueHandler.handleReturnValue(successResult, methodParameter, modelAndViewContainer, nativeWebRequest);
        }


    }
}
