package com.hk.remark.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hk.remark.common.constants.RedisConstants;
import com.hk.remark.common.constants.ReqRespConstants;
import com.hk.remark.common.resp.ResponseResult;
import com.hk.remark.common.util.ResourceHolder;
import com.hk.remark.common.util.SystemConstants;
import com.hk.remark.dto.ScrollResult;
import com.hk.remark.dto.UserDTO;
import com.hk.remark.entity.BlogPO;
import com.hk.remark.entity.FollowPO;
import com.hk.remark.entity.UserPO;
import com.hk.remark.mapper.BlogMapper;
import com.hk.remark.mapstruct.UserMapStructure;
import com.hk.remark.service.IBlogService;
import com.hk.remark.service.IFollowService;
import com.hk.remark.service.IUserService;
import com.hk.remark.service.config.CacheService;
import com.hk.remark.service.util.RedisDBChangeUtils;
import com.hk.remark.vo.UserVO;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author : HK意境
 * @ClassName : BlogServiceImpl
 * @date : 2022/10/26 13:18
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, BlogPO> implements IBlogService {

    @Resource
    private IUserService userService;
    @Resource
    private IFollowService followService;
    @Resource
    private RedisDBChangeUtils redisDBChangeUtils;


    @Override
    public ResponseResult queryBlogById(Long blogId) {

        // 查询探店博文
        BlogPO blogPO = this.getById(blogId);
        if (Objects.isNull(blogPO)) {
            // 博文不存在
            return ResponseResult.FAIL("非常抱歉，您想要的探店博文不见了");
        }

        // 查询用户信息
        this.queryBlog(blogPO);
        this.isBlogLiked(blogPO);

        return ResponseResult.SUCCESS(blogPO);
    }


    /**
     * 分页查询热点博文
     *
     * @param current
     *
     * @return
     */
    @Override
    public ResponseResult queryHotBlog(Integer current) {

        // 根据用户查询
        Page<BlogPO> page = this.lambdaQuery().orderByDesc(BlogPO::getLiked).page(Page.of(current, SystemConstants.MAX_PAGE_SIZE));

        // 获取当前页数据
        List<BlogPO> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlog(blog);
            this.isBlogLiked(blog);
        });

        return ResponseResult.SUCCESS(records);
    }


    /**
     * 点赞探店博文
     *
     * @param blogId
     *
     * @return
     */
    @Override
    public ResponseResult likeBlog(Long blogId) {

        // 1. 判断当前登录用户是否点赞
        // 获取当前用户
        UserPO user = (UserPO) ResourceHolder.get(ReqRespConstants.USER);

        // 查询用户点赞记录
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.BLOG_LIKED_KEY);
        String blogKey = RedisConstants.BLOG_LIKED_KEY + blogId;
        // 通过 redis set 集合判断是否点赞
        //Boolean isLiked = redisTemplate.opsForSet().isMember(blogKey, user.getId().toString());
        Double score = redisTemplate.opsForZSet().score(blogKey, user.getId().toString());

        // 判断用户是否点赞
        if (Objects.nonNull(score)) {
            // 已经点赞，取消点赞，点赞数减一
            // redisTemplate.opsForSet().remove(blogKey, user.getId().toString());
            redisTemplate.opsForZSet().remove(blogKey, user.getId().toString());

            // 异步任务去取消点赞
            CacheService.cacheThreadPoolExecutor.submit((Callable<Object>) () -> {
                try {
                    return lambdaUpdate().setSql("liked = liked - 1").eq(BlogPO::getId, blogId).update();
                } catch (Exception e) {
                    // 点赞失败
                    return false;
                }
            });

            // 点赞成功
            return ResponseResult.SUCCESS("取消点赞成功");
        }

        // 未点赞，用户可以进行点赞，移除redis 点赞列表中的用户
        //redisTemplate.opsForSet().add(blogKey, user.getId().toString());
        redisTemplate.opsForZSet().add(blogKey, user.getId().toString(), System.currentTimeMillis());

        // 异步任务去做点赞
        CacheService.cacheThreadPoolExecutor.submit((Callable<Object>) () -> {
            try {
                return lambdaUpdate().setSql("liked = liked + 1").eq(BlogPO::getId, blogId).update();
            } catch (Exception e) {
                // 点赞失败
                return false;
            }
        });

        return ResponseResult.SUCCESS("点赞成功");
    }


    /**
     * 查询摊点博文的点赞用户信息
     *
     * @param id
     *
     * @return ResponseResult
     */
    @Override
    public ResponseResult queryBlogLikes(Long id) {
        // 查询top 5 点赞用户
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.BLOG_LIKED_KEY);
        String blogKey = RedisConstants.BLOG_LIKED_KEY + id;

        // top 5 点赞用户id
        Set<String> userIdSet = redisTemplate.opsForZSet().range(blogKey, 0L, 4L);
        if (CollectionUtil.isEmpty(userIdSet)) {
            // 点赞集合为空
            return ResponseResult.SUCCESS(Lists.newArrayList());
        }

        // 反查用户信息
        List<Long> userIdList = userIdSet.stream().map(Long::valueOf).collect(Collectors.toList());

        List<UserVO> likedUserList = this.userService.listByIds(userIdList)
                .stream().map(UserMapStructure.INSTANCE::userPO2UserVO).collect(Collectors.toList());

        return ResponseResult.SUCCESS(likedUserList);
    }


    /**
     * 发布探店博文，主动推送给关注者
     * @param blog
     * @return
     */
    @Override
    public ResponseResult publishBlog(BlogPO blog) {

        // 获取登录用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean save = this.save(blog);

        // 判断探店博文保存是否成功
        if (BooleanUtils.isFalse(save)) {
            // 保存失败
            return ResponseResult.FAIL();
        }

        // 编辑，发布探店博文成功
        // 推送到关注者的信箱
        CacheService.cacheThreadPoolExecutor.submit(() -> {
            StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.FEED_KEY);
            // 查询followers
            List<FollowPO> followers = followService.getFollowers(user.getId());
            // 推送探店博文到关注者的收件箱
            followers.forEach(followPO ->{
                // 关注者id
                Long followUserId = followPO.getFollowUserId();
                // 推送到收件箱
                redisTemplate.opsForZSet().add(RedisConstants.FEED_KEY + followUserId,
                        blog.getId().toString(), System.currentTimeMillis());
            });
        });

        // 响应博文
        return ResponseResult.SUCCESS(blog);
    }


    /**
     * 从用户信箱里面获取探店博文
     * @param max
     * @param offset
     * @return
     */
    @Override
    public ResponseResult queryBolgOfSubscribe(Long max, Integer offset) {

        // 获取当前用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);

        // 获取收件箱
        String key = RedisConstants.FEED_KEY + user.getId().toString();
        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.BLOG_LIKED_KEY);
        // 获取feed 流
        Set<ZSetOperations.TypedTuple<String>> subscribeBlogsWithScores = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(
                        // key
                        key,
                        // 发布时间最小值
                        0,
                        // 发布时间最大值: 上一次查询的最后一篇博文
                        max,
                        // 偏移量
                        offset,
                        // 查询博文数量
                        6
                );
        if (CollectionUtil.isEmpty(subscribeBlogsWithScores)) {
            // 发布博文为空
            return ResponseResult.SUCCESS(new ScrollResult());
        }

        // 解析数据：blogId, minTime, offset,
        List<Long> blogIds = Lists.newArrayListWithCapacity(16);
        long minTime = 0;
        // 和 minTime 分数一样的 博文个数
        int minTimeCount = 1;

        // 解析数据
        for (ZSetOperations.TypedTuple<String> tuple : subscribeBlogsWithScores) {
            // 获取blogId
            blogIds.add(Long.valueOf(tuple.getValue()));
            // 获取score: 时间戳
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                minTimeCount++;
            } else {
                minTime = time;
                minTimeCount = 1;
            }
        }

        // 查询博文数据，封装，返回
        List<BlogPO> blogPOList = this.listByIds(blogIds);
        blogPOList.forEach(blog -> {
            this.queryBlog(blog);
            this.isBlogLiked(blog);
        });
        // 封装
        ScrollResult<BlogPO> scrollResult = new ScrollResult<BlogPO>().setList(blogPOList).setMinTime(minTime).setOffset(minTimeCount);

        return ResponseResult.SUCCESS(scrollResult);
    }


    /**
     * 查询探店博文用户信息
     *
     * @param blog
     */
    private void queryBlog(BlogPO blog) {

        // user 信息
        Long userId = blog.getUserId();
        UserPO user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    private void isBlogLiked(BlogPO blog) {
        // 当前点赞用户
        UserVO user = (UserVO) ResourceHolder.get(ReqRespConstants.USER);
        if (Objects.isNull(user)) {
            // 用户未登录，无需查询点赞状态
            return;
        }

        StringRedisTemplate redisTemplate = redisDBChangeUtils.getStringRedisTemplate(RedisConstants.BLOG_LIKED_KEY);
        // 判断是否点赞成员列表
        // Boolean isLiked = redisTemplate.opsForSet().isMember(RedisConstants.BLOG_LIKED_KEY + blog.getId(), user.getId().toString());
        Double score = redisTemplate.opsForZSet().score(RedisConstants.BLOG_LIKED_KEY + blog.getId(), user.getId().toString());

        blog.setIsLike(Objects.nonNull(score));
    }

}
