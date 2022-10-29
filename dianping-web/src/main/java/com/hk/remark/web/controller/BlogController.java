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
        // 获取登录用户
        UserDTO user = (UserDTO) ResourceHolder.get(ReqRespConstants.USER);
        blog.setUserId(user.getId());
        // 保存探店博文
        blogService.save(blog);
        // 返回id
        return ResponseResult.SUCCESS(blog.getId());
    }

    @PutMapping("/like/{id}")
    public ResponseResult likeBlog(@PathVariable("id") Long id) {
        // 修改点赞数量
        blogService.update()
                .setSql("liked = liked + 1").eq("id", id).update();
        return ResponseResult.SUCCESS();
    }

    @GetMapping("/of/me")
    public ResponseResult queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = (UserDTO) ResourceHolder.get(ReqRespConstants.USER);

        // 根据用户查询
        Page<BlogPO> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<BlogPO> records = page.getRecords();
        return ResponseResult.SUCCESS(records);
    }

    @GetMapping("/hot")
    public ResponseResult queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据用户查询
        Page<BlogPO> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<BlogPO> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            UserPO user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
        return ResponseResult.SUCCESS(records);
    }
}
