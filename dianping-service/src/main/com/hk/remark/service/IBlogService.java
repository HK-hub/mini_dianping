package com.hk.remark.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.entity.BlogPO;

/**
 * @ClassName : IBlogService
 * @author : HK意境
 * @date : 2022/10/26 13:22
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface IBlogService extends IService<BlogPO> {

    // 查询 blog
    ResponseResult queryBlogById(Long blogId);

    // 分页查询 热点探店博文
    ResponseResult queryHotBlog(Integer current);

    // 点赞探店博文
    ResponseResult likeBlog(Long id);

    // 查询摊点博文的点赞用户
    ResponseResult queryBlogLikes(Long id);

    // 发布探店博文
    ResponseResult publishBlog(BlogPO blog);

    // 获取关注者发布的探店博文
    ResponseResult queryBolgOfSubscribe(Long max, Integer offset);
}
