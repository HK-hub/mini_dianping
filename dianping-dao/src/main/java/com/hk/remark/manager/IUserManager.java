package com.hk.remark.manager;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.hk.remark.entity.UserPO;

import java.util.List;

/**
 * @author : HK意境
 * @ClassName : IUserManager
 * @date : 2022/10/28 12:58
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IUserManager {

    public UserPO saveUserPO(UserPO user);

    public UserPO saveOrUpdateUserPO(UserPO user);

    public UserPO updateUserPO(UserPO userPO);

    public List<UserPO> queryUsers(LambdaQueryChainWrapper<UserPO> queryWrapper);


    }
