package com.hk.remark.web.config;

import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.service.util.RedisDBChangeUtils;
import com.hk.remark.web.interceptor.LoginInterceptor;
import com.hk.remark.web.interceptor.TraceInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author : HK意境
 * @ClassName : CustomWebMvcConfigurer
 * @date : 2022/10/27 20:58
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加 trance 相关拦截器
        TraceInterceptor traceInterceptor = new TraceInterceptor();
        registry.addInterceptor(traceInterceptor);

        // 添加 login 相关拦截器
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.LOGIN_USER_KEY);
        LoginInterceptor loginInterceptor = new LoginInterceptor(redisTemplate);
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/user/code",   // 验证码
                        "/user/login",  // 登录
                        "/blog/hot",    // 热点日记
                        "/shop/**",     // 店铺
                        "/shop-type/**",    // 店铺类型
                        "/upload/**",   // 文件上传
                        "/voucher/**"   // 优惠卷
                );

    }



}
