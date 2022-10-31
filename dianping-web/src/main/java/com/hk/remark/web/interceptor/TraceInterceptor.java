package com.hk.remark.web.interceptor;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import com.hk.remark.common.constants.ReqRespConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : HK意境
 * @ClassName : TraceInterceptor
 * @date : 2022/10/27 19:29
 * @description : 登录 和 链路追踪 等功能的拦截器
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
public class TraceInterceptor implements HandlerInterceptor {

    // 开始 链路追踪
    // 用ThreadLocal记录当前线程访问接口的开始时间
    private ThreadLocal<Map<String,Object>> stopwatchThreadLocal = new ThreadLocal<>();
    private static final String TIMER = "stopWatch";

    // 请求 handler 请求前置处理
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Map<String, Object> hashMap = MapUtil.newHashMap();

        // 设置 tranceId
        String traceId = request.getHeader(ReqRespConstants.TRACE_ID);

        if (StringUtils.isEmpty(traceId)) {
            traceId = UUID.fastUUID().toString();
            //log.info("request traceId is empty,create by random={}",traceId);
        }
        // 设置响应头
        response.setHeader(ReqRespConstants.TRACE_ID, traceId);
        hashMap.put(ReqRespConstants.TRACE_ID,traceId);

        // 开始业务计时
        StopWatch stopWatch = StopWatch.create(traceId);
        stopWatch.start();
        hashMap.put(TIMER,stopWatch);

        // 放入资源
        stopwatchThreadLocal.set(hashMap);

        return Boolean.TRUE;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        // 日志, 追踪
        StopWatch stopWatch = (StopWatch) stopwatchThreadLocal.get().get(TIMER);
        stopWatch.stop();
        String traceId = (String) stopwatchThreadLocal.get().get(ReqRespConstants.TRACE_ID);

        HandlerMethod method = (HandlerMethod) handler;
        String methodName = method.getBeanType()+"#"+method.getMethod().getName();
        String parameters = Arrays.toString(method.getMethod().getParameterTypes());
        String returnType = method.getMethod().getReturnType().toString();
        Long executeTime = stopWatch.getTotalTimeMillis();

        // 释放
        stopwatchThreadLocal.remove();

        // 日志
        //log.info("traceId={},method={},parameters={},return type={},result={},execute time={}",
        //       traceId, methodName, parameters, returnType, response.getStatus(), executeTime);

    }

    // post 后置处理完成后，没有异常才会执行完成方法
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
