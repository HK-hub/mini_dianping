package com.hk.remark.web.config;

import com.hk.remark.web.interceptor.ResultHandlerMethodReturnValueHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : HK意境
 * @ClassName : ReturnValueConfig
 * @date : 2022/10/27 11:51
 * @description : 返回值配置类
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Configuration
public class ReturnValueConfig implements InitializingBean {

    @Resource
    private RequestMappingHandlerAdapter adapter ;

    @Override
    public void afterPropertiesSet() throws Exception {

        // 获取原始 返回值处理器 集合
        List<HandlerMethodReturnValueHandler> originalHandlers = adapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>(originalHandlers.size());

        // 注册自定义返回值处理器
        for (HandlerMethodReturnValueHandler originHandler : originalHandlers) {
            if (originHandler instanceof RequestResponseBodyMethodProcessor) {
                newHandlers.add(new ResultHandlerMethodReturnValueHandler(originHandler));
            }else{
                newHandlers.add(originHandler);
            }
        }
        adapter.setReturnValueHandlers(newHandlers);

    }
}
