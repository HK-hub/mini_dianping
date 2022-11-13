package com.hk.remark.web.controller;


import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.service.IFollowService;
import com.hk.remark.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author : HK意境
 * @ClassName : FollowController
 * @date : 2022/10/26 16:32
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    /**
     * 关注博主或者取消关注
     *
     * @param upId 博主id
     * @param opt  操作：关注 or 取关
     *
     * @return
     */
    @PutMapping("/{id}/{opt}")
    public ResponseResult follow(@PathVariable(name = "id") Long upId, @PathVariable(name = "opt") Boolean opt) {

        // 关注或者取消关注
        return this.followService.followOrCancel(upId, opt);
    }


    /**
     * @param upId 博主id
     *
     * @return ResponseResult
     *
     * @methodName : isFollow
     * @author : HK意境
     * @date : 2022/11/12 18:12
     * @description : 当前用户是否关注博主
     * @Todo :
     * @apiNote : 前用户是否关注博主
     * @params :
     * @throws:
     * @Bug :
     * @Modified :
     * @Version : 1.0.0
     */
    @GetMapping("/isFlollower/{upId}")
    public ResponseResult isFollow(@PathVariable(name = "upId") Long upId) {

        // 判断是否关注
        return this.followService.isFollower(upId);
    }


    /**
     * 查看当前用户和博主的共同关注列表
     * @param upId
     * @return
     */
    @GetMapping("/intersection/{upId}")
    public ResponseResult intersectFollow(@PathVariable(name = "upId") Long upId) {

        // 获取当前用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        // 查询共同关注用户列表
        return this.followService.intersectFollowers(upId, user.getId());
    }





}
