package com.hk.remark.service;

import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.entity.FollowPO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @ClassName : IFollowService
 * @author : HK意境
 * @date : 2022/11/12 17:42
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IFollowService extends IService<FollowPO> {

    // 关注 or 取关
    ResponseResult followOrCancel(Long upId, Boolean opt);

    // 是否关注
    ResponseResult isFollower(Long upId);

    // 两个用户的共同关注对象
    ResponseResult intersectFollowers(Long upId, Long id);

    // 获取博主的关注者列表
    List<FollowPO> getFollowers(Long id);
}
