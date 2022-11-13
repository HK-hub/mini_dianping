package com.hk.remark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.dto.LoginFormDTO;
import com.hk.remark.entity.UserPO;

import java.util.Map;

/**
 * @ClassName : IUserService
 * @author : HK意境
 * @date : 2022/10/27 21:33
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IUserService extends IService<UserPO> {

    ResponseResult sendCode(String phone);

    ResponseResult login(LoginFormDTO loginForm) throws Exception;

    ResponseResult getUserProfile();

    // 登录用户后置处理
    Map<String,Object> loginPostProcess(UserPO userPO) throws Exception;

    // 登录前置处理
    ResponseResult loginPreProcess(LoginFormDTO loginForm);
}
