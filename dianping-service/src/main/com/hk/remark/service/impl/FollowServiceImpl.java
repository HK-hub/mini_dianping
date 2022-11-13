package com.hk.remark.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.entity.FollowPO;
import com.hk.remark.mapper.FollowMapper;
import com.hk.remark.mapstruct.UserMapStructure;
import com.hk.remark.service.IFollowService;
import com.hk.remark.service.IUserService;
import com.hk.remark.service.config.CacheService;
import com.hk.remark.vo.UserVO;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName : FollowServiceImpl
 * @author : HK意境
 * @date : 2022/10/26 13:21
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, FollowPO> implements IFollowService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    /**
     * 关注或取关
     * @param upId 博主id
     * @param opt 操作：true-> 关注，false-> 取关
     * @return
     */
    @Override
    public ResponseResult followOrCancel(Long upId, Boolean opt) {

        // 获取当前用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        // 关注列表
        String key = RedisConstants.UP_FOLLOW_KEY + user.getId();

        // 判断操作: 通过异步任务去加快响应速度
        if (BooleanUtils.isTrue(opt)) {
            // 关注博主
            CacheService.cacheThreadPoolExecutor.submit(() -> {
                // 保存关注记录
                save(new FollowPO().setUserId(upId).setFollowUserId(user.getId()));
                // 添加关注用户
                stringRedisTemplate.opsForSet().add(key, user.getId().toString());
            });

        } else {
            // 取关
            CacheService.cacheThreadPoolExecutor.submit(() -> {
                // 保存关注记录
                boolean remove = this.lambdaUpdate()
                        .eq(FollowPO::getFollowUserId, user.getId())
                        .eq(FollowPO::getUserId, upId)
                        .remove();
                // 是否存在关注记录
                if (BooleanUtils.isTrue(remove)) {
                    // 删除成功
                    stringRedisTemplate.opsForSet().remove(key, user.getId().toString());
                }
            });
        }

        // 响应结果
        return ResponseResult.SUCCESS(BooleanUtils.isTrue(opt) ? "关注成功" : "取消关注成功");
    }

    /**
     * 是否关注
     * @param upId
     * @return
     */
    @Override
    public ResponseResult isFollower(Long upId) {

        // 获取当前用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);

        // 查询关注记录
        Integer follower = this.lambdaQuery()
                .eq(FollowPO::getFollowUserId, user.getId())
                .eq(FollowPO::getUserId, upId)
                .count();

        // 响应对象
        return ResponseResult.SUCCESS(follower > 0);
    }


    /**
     * 两个用户的共同关注列表
     * @param upId
     * @param upId, userId
     * @return
     */
    @Override
    public ResponseResult intersectFollowers(Long upId, Long userId) {

        // 两个需求求交集的用户列表
        String upKey = RedisConstants.UP_FOLLOW_KEY + upId;
        String userKey = RedisConstants.UP_FOLLOW_KEY + userId;

        // 求交集
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(upKey, userKey);
        if (CollectionUtil.isEmpty(intersect)) {
            // 交集为空
            return ResponseResult.SUCCESS(Lists.newArrayList());
        }

        // 查询共同关注用户信息
        List<Long> userIdList = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserVO> intersectionUserList = this.userService.listByIds(userIdList)
                .stream().map(UserMapStructure.INSTANCE::userPO2UserVO)
                .collect(Collectors.toList());

        // 响应共同关注用户列表
        return ResponseResult.SUCCESS(intersectionUserList);
    }


    /**
     * 获取博主的关注者集合
     * @param upId 博主
     * @return
     */
    @Override
    public List<FollowPO> getFollowers(Long upId) {

        // 查询博主的关注者
        List<FollowPO> followPOList = this.lambdaQuery().eq(FollowPO::getUserId, upId).list();

        return followPOList;
    }


}





