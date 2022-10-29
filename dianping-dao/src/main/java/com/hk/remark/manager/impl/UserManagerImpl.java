package com.hk.remark.manager.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.google.common.collect.Lists;
import com.hk.remark.entity.UserPO;
import com.hk.remark.manager.IUserManager;
import com.hk.remark.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author : HK意境
 * @ClassName : UserManagerImpl
 * @date : 2022/10/28 13:00
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Component
public class UserManagerImpl implements IUserManager {

    @Resource
    private UserMapper userMapper;


    /**
     * 插入用户
     * @param userPO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPO saveUserPO(UserPO userPO) {
        // 插入用户
        int insert = userMapper.insert(userPO);

        // 判断是否插入成功
        if (insert <= 0) {
            return null;
        }
        return userPO;
    }


    /**
     * 插入或更新用户
     * @param user
     * @return
     */
    @Override
    public UserPO saveOrUpdateUserPO(UserPO user) {

        // 计数:查询用户是否存在
        LambdaQueryChainWrapper<UserPO> wrapper = new LambdaQueryChainWrapper<>(this.userMapper);
        wrapper.eq(UserPO::getId, user.getId());
        Integer count = this.userMapper.selectCount(wrapper);

        UserPO result = null;
        if (count > 0) {
            // 用户存在：更新用户
            result = this.updateUserPO(user);

        }else {
            // 用户不存在：插入用户
            result = this.saveUserPO(user);
        }

        return result;
    }


    /**
     * 更新用户
     * @param userPO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPO updateUserPO(UserPO userPO) {

        // 返回更新的数量
        int update = this.userMapper.updateById(userPO);

        return userPO;
    }


    /**
     * 根据查询条件 查询用户集合
     * @param queryWrapper
     * @return
     */
    @Override
    public List<UserPO> queryUsers(LambdaQueryChainWrapper<UserPO> queryWrapper){
        // 查询
        //ist<UserPO> userList = this.userMapper.selectList(queryWrapper);

        List<UserPO> userList = queryWrapper.list();
        // 查询结果封装
        if (CollectionUtil.isEmpty(userList)) {
            userList = Lists.newArrayList();
        }
        return userList;
    }


}
