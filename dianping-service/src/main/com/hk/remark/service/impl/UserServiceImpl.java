package com.hk.remark.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.resp.ResultCode;
import com.hk.remark.common.util.*;
import com.hk.remark.dto.LoginFormDTO;
import com.hk.remark.entity.UserPO;
import com.hk.remark.manager.IUserManager;
import com.hk.remark.mapper.UserMapper;
import com.hk.remark.mapstruct.UserMapStructure;
import com.hk.remark.service.IUserService;
import com.hk.remark.service.util.RedisDBChangeUtils;
import com.hk.remark.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author : HK意境
 * @ClassName : UserServiceImpl
 * @date : 2022/10/27 21:23
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j(topic = "UserService")
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements IUserService {

    @Resource
    private IUserManager userManager;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;


    /**
     * 发送手机验证码
     *
     * @param phone
     *
     * @return
     */
    @Override
    public ResponseResult sendCode(String phone) {

        // 1. 校验数据
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 手机号格式错误
            return new ResponseResult(ResultCode.BAD_REQUEST, "手机号格式错误!");
        }
        // 2. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 保存验证码
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.LOGIN_CODE_KEY);
        redisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code,
                RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 4. 发送验证码
        log.info("短信验证码发送成功->user:{},code={}", phone, code);


        return ResponseResult.SUCCESS("短信验证码发送成功!"+code);
    }


    /**
     * 用户登录
     * // TODO: 2022/10/27 校验提交号码和短信发送号码是否一致
     *
     * @param loginForm
     *
     * @return
     */
    @Override
    public ResponseResult login(LoginFormDTO loginForm) throws Exception {

        // 前置校验
        ResponseResult result = this.loginPreProcess(loginForm);
        if (Objects.equals(result.isSuccess(), Boolean.FALSE)) {
            // 前置校验失败
            return result;
        }

        // 3. 查询用户
        String phone = loginForm.getPhone();
        List<UserPO> userPOList = this.userManager.queryUsers(this.lambdaQuery().eq(UserPO::getPhone, phone));
        UserPO userPO = userPOList.isEmpty() ? null : userPOList.get(0);

        // 判断用户是否存在
        if (CollectionUtil.isEmpty(userPOList)) {
            // 用户不存在, 创建用户
            userPO = this.createUserWithPhone(phone);

            // 插入用户是否成功
            if (Objects.isNull(userPO)) {
                // 插入用户失败
                return ResponseResult.FAIL("注册用户失败,请稍后重试!");
            }
        }

        // 4. 保存用户信息到redis 中
        Map<String, Object> map = this.loginPostProcess(userPO);

        // 4. 保存用户登录token
        log.info("user token:{}->{}", phone, map.get(ReqRespConstants.USER_TOKEN));

        // 5. 响应结果
        return ResponseResult.SUCCESS(map.get(ReqRespConstants.USER_TOKEN));
    }


    /**
     * 查询用户个人信息
     *
     * @return
     */
    @Override
    public ResponseResult getUserProfile() {
        // 通过 ResourceHolder 取个人信息
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);

        return ResponseResult.SUCCESS(user);
    }


    /**
     * 用户登录后置处理：保存用户信息，token等
     *
     * @param userPO
     *
     * @return
     */
    @Override
    public Map<String, Object> loginPostProcess(UserPO userPO) throws Exception {

        // 获取 payload
        UserVO userVO = UserMapStructure.INSTANCE.userPO2UserVO(userPO);
        Map<String, String> stringMap = UserVO.toStringMap(userVO);
        // 生成 token
        String token = JWTUtil.createToken(JSONUtil.toJsonStr(userVO), RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);

        // 保存 token 到 redis
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.LOGIN_USER_KEY);
        redisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, stringMap);

        // 设置过期时间
        redisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);

        // 返回 token
        return MapUtil.of(ReqRespConstants.USER_TOKEN, token);
    }

    /**
     * 用户登录前置处理：校验，验证
     *
     * @param loginForm
     *
     * @return
     */
    @Override
    public ResponseResult loginPreProcess(LoginFormDTO loginForm) {
        // 1. 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 手机号格式错误
            return new ResponseResult(ResultCode.BAD_REQUEST, "手机号格式错误!");
        }

        // 2. 校验验证码
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.LOGIN_CODE_KEY);
        String cacheCode = redisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String submitCode = loginForm.getCode();

        if (!Objects.equals(cacheCode, submitCode)) {
            // 验证码错误
            return new ResponseResult(ResultCode.BAD_REQUEST, "手机号或验证码错误!");
        }
        // 验证码校验成功后应该删除
        redisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);

        // 手机号校验成功，验证码校验通过
        return ResponseResult.SUCCESS();
    }


    // 根据 phone 创建用户
    public UserPO createUserWithPhone(String phone) {

        // 构造数据
        UserPO userPO = new UserPO();
        userPO.setPhone(phone);
        userPO.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(8));

        // 插入用户
        UserPO result = this.userManager.saveUserPO(userPO);

        // 响应
        return result;
    }


}
