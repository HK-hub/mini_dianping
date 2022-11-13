package com.hk.remark.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.util.SystemConstants;
import com.hk.remark.dto.UserDTO;
import com.hk.remark.entity.BlogPO;
import com.hk.remark.entity.UserPO;
import com.hk.remark.service.IBlogService;
import com.hk.remark.service.IUserService;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.vo.UserVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : BlogController
 * @author : HK意境
 * @date : 2022/10/27 18:55
 * @description : 探店日记，文章
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    @PostMapping
    public ResponseResult saveBlog(@RequestBody BlogPO blog) {

        // 发布探店博文
        ResponseResult result = this.blogService.publishBlog(blog);
        // 返回id
        return ResponseResult.SUCCESS(blog.getId());
    }


    @GetMapping("/{id}")
    public ResponseResult queryBlogById(@PathVariable(name = "id") Long blogId) {
        return this.blogService.queryBlogById(blogId);
    }


    @PutMapping("/like/{id}")
    public ResponseResult likeBlog(@PathVariable("id") Long id) {
        // 修改点赞数量
        ResponseResult result = this.blogService.likeBlog(id);
        return result;
    }


    /**
     * 查询我的探店博文
     * @param current
     * @return
     */
    @GetMapping("/of/me")
    public ResponseResult queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);

        // 根据用户查询
        Page<BlogPO> page = blogService.query()
                .eq("user_id", user.getId())
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<BlogPO> records = page.getRecords();
        return ResponseResult.SUCCESS(records);
    }

    /**
     * 查询我关注的博主的探店博文
     * @param max 上一次获取到的最后一篇探店博文
     * @param offset 距离上一批次博文的偏移量
     * @return ResponseResult
     */
    @GetMapping("/of/subscribe")
    public ResponseResult querySubscribeBlog(@RequestParam(name = "lastId") Long max,
                                             @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset) {

        return this.blogService.queryBolgOfSubscribe(max, offset);
    }


    /**
     * 查询热点探店博文
     * @param current
     * @return
     */
    @GetMapping("/hot")
    public ResponseResult queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return this.blogService.queryHotBlog(current);
    }


    /**
     * 点赞探店博文
     * @param id
     * @return
     */
    @GetMapping("/likes/{id}")
    public ResponseResult queryBlogLikes(@RequestParam(value = "id") Long id) {

        return this.blogService.queryBlogLikes(id);
    }


}
