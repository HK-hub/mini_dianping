package com.hk.remark.common.error;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author : HK意境
 * @ClassName : BaseException
 * @date : 2022/10/26 21:47
 * @description : 异常基类
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BaseException extends Exception{

    // 链路追踪 id
    private String traceId ;
    // 错误/异常 消息
    protected String message ;
    // 错误/异常 原因
    protected String causes ;
    // 错误/异常 代码
    protected Integer code ;
    // 错误/异常 类型
    protected ExceptionType type ;
    // 错误/异常 对象
    protected Exception exceptionObject ;


    public static enum ExceptionType {

        // 异常
        Exception("exception"),
        // 运行时异常
        RuntimeException("runtimeException"),
        // 自定义异常
        CustomException("customException"),
        // 业务异常
        BusinessException("businessException"),
        // 链路异常(请求环节出现异常:参数，路径，方法等)
        RequestException("requestException"),
        // 服务异常
        ServiceException("serviceException")

        ;

        // 异常类型
        String type ;

        ExceptionType(String type) {
            this.type = type;
        }

        ExceptionType() {
        }
    }


}
