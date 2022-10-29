package com.hk.remark.web.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.error.RequestException;
import com.hk.remark.common.resp.ResultCode;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : HK意境
 * @ClassName : LoginInterceptor
 * @date : 2022/10/28 11:49
 * @description : 刷新 token 拦截器，只做刷新，不做拦截
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
public class RefreshInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public RefreshInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取 token
        String token = request.getHeader(ReqRespConstants.USER_TOKEN);
        if (StringUtils.isEmpty(token)) {
            // 未登录，放行
            return Boolean.TRUE;
        }

        // 校验登录态
        //验证令牌  如果令牌不正确会出现异常 被全局异常处理
        // DecodedJWT verify = JWTUtil.verify(token);

        // 查询用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<String, Object> entries = redisTemplate.opsForHash().entries(key);
        if (MapUtil.isEmpty(entries)) {
            // 未登录，放行
            return Boolean.TRUE;
        }

        // 登录用户转换数据
        UserVO userVO = BeanUtil.fillBeanWithMap(entries, new UserVO(), false);
        log.info("entry={},userVo={}",entries,userVO);

        // 刷新token
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        // 过期时间小于五分钟
        if (expire > 0 && expire <= 60 * 5){
            // token 续约
            redisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL,TimeUnit.SECONDS);
        }

        // 设置 threadlocal
        ResourceHolder.save(ReqRespConstants.USER, userVO);

        return Boolean.TRUE;
    }



    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 释放资源
        ResourceHolder.remove();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 避免 postHandle 方法没有执行释放资源，再次释放
        ResourceHolder.remove();
    }
}




