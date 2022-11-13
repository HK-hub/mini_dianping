package com.hk.remark.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.entity.UserInfoPO;
import com.hk.remark.mapper.UserInfoMapper;
import com.hk.remark.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-24
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfoPO> implements IUserInfoService {

}
