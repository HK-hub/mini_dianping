package com.hk.remark.web.interceptor;

import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.error.RequestException;
import com.hk.remark.common.resp.ResultCode;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;


/**
 * @author : HK意境
 * @ClassName : LoginInterceptor
 * @date : 2022/10/28 11:49
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断是否需要拦截
        UserVO userVO = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        if (Objects.isNull(userVO)) {
            // 用户未登录，拦截
            System.out.println(request.getRequestURI());
            System.out.println(request.getHeader("user-agent"));
            throw new RequestException(ResultCode.UNAUTHORIZED);
        }

        // 已经登录 放行
        return Boolean.TRUE;
    }
}




