package com.hk.remark.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hk.remark.entity.UserPO;
import com.hk.remark.mapper.UserMapper;
import com.hk.remark.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements IUserService {

}
