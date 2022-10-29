package com.hk.remark.web.controller;


import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.dto.LoginFormDTO;
import com.hk.remark.dto.Result;
import com.hk.remark.entity.UserInfoPO;
import com.hk.remark.service.IUserInfoService;
import com.hk.remark.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author : HK意境
 * @ClassName : UserController
 * @date : 2022/10/27 19:08
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * @param phone 用户手机号
     *
     * @return ResponseResult
     *
     * @methodName : sendCheckCode
     * @author : HK意境
     * @date : 2022/10/27 21:21
     * @description : 用户登录注册验证码
     * @Todo :
     * @apiNote : 发送用户登录注册验证码
     * @params :
     * @throws:
     * @Bug :
     * @Modified :
     * @Version : 1.0.0
     */
    @PostMapping("/code")
    public ResponseResult sendCheckCode(@RequestParam("phone") String phone) {

        return userService.sendCode(phone);

    }

    /**
     * @methodName : login
     * @author : HK意境
     * @date : 2022/10/27 21:53
     * @description : 用户登录
     * @Todo :
     * @apiNote : 用户登录
     * @params :
         * @param loginForm 登录请求体: 包括手机号,验证码; 手机号,密码
     * @return ResponseResult
     * @throws:
     * @Bug :
     * @Modified :
     * @Version : 1.0.0
     */
    @PostMapping("/login")
    public ResponseResult login(@RequestBody LoginFormDTO loginForm) throws Exception {

        return userService.login(loginForm);
    }

    /**
     * 登出功能
     *
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout() {
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    /**
     * @methodName : profile
     * @author : HK意境
     * @date : 2022/10/28 19:25
     * @description : 查询个人相关信息
     * @Todo :
     * @apiNote : 查询个人相关信息
     * @params :
     * @return ResponseResult
     * @throws:
     * @Bug :
     * @Modified :
     * @Version : 1.0.0
     */
    @GetMapping("/me")
    public ResponseResult profile() {
        // TODO 获取当前登录的用户并返回
        ResponseResult result = this.userService.getUserProfile();
        return result;
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfoPO info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }
}
