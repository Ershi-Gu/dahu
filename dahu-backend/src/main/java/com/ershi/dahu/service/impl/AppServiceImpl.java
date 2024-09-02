package com.ershi.dahu.service.impl;

import java.time.LocalDateTime;
import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.common.ReviewRequest;
import com.ershi.dahu.constant.CommonConstant;
import com.ershi.dahu.exception.ThrowUtils;
import com.ershi.dahu.mapper.AppMapper;
import com.ershi.dahu.model.dto.app.AppQueryRequest;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.RedisData;
import com.ershi.dahu.model.entity.User;
import com.ershi.dahu.model.enums.AppTypeEnum;
import com.ershi.dahu.model.enums.ReviewStatusEnum;
import com.ershi.dahu.model.enums.ScoringStrategyEnum;
import com.ershi.dahu.model.vo.AppVO;
import com.ershi.dahu.model.vo.UserVO;
import com.ershi.dahu.service.AppService;
import com.ershi.dahu.service.UserService;
import com.ershi.dahu.utils.SqlUtils;
import com.ershi.dahu.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ershi.dahu.constant.RedisCacheConstant.*;

/**
 * 应用表服务实现
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 校验数据
     *
     * @param app
     * @param add 对创建的数据进行校验
     */
    @Override
    public void validApp(App app, boolean add) {

        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        String appIcon = app.getAppIcon();
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        Integer reviewStatus = app.getReviewStatus();
        String reviewMessage = app.getReviewMessage();
        Long reviewerId = app.getReviewerId();
        Date reviewTime = app.getReviewTime();
        Long userId = app.getUserId();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(appName), ErrorCode.PARAMS_ERROR, "应用名不能为空");
            ThrowUtils.throwIf(StringUtils.isAnyBlank(appDesc), ErrorCode.PARAMS_ERROR, "应用描述不能为空");
            AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(appType);
            ThrowUtils.throwIf(appTypeEnum == null, ErrorCode.PARAMS_ERROR, "应用类别非法");
            ScoringStrategyEnum scoringStrategyEnum = ScoringStrategyEnum.getEnumByValue(scoringStrategy);
            ThrowUtils.throwIf(scoringStrategyEnum == null, ErrorCode.PARAMS_ERROR, "应用评分策略非法");
        }

        // 补充校验规则
        if (StringUtils.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() >= 20, ErrorCode.PARAMS_ERROR, "应用名称应当小于20");
        }
        if (StringUtils.isNotBlank(appDesc)) {
            ThrowUtils.throwIf(appDesc.length() >= 80, ErrorCode.PARAMS_ERROR, "应用描述应当小于80");
        }
        if (reviewStatus != null) {
            ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
            ThrowUtils.throwIf(reviewStatusEnum == null, ErrorCode.PARAMS_ERROR, "审核状态非法");
        }

    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {

        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = appQueryRequest.getId();
        Long notId = appQueryRequest.getNotId();
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        String reviewMessage = appQueryRequest.getReviewMessage();
        Long reviewerId = appQueryRequest.getReviewerId();
        Long userId = appQueryRequest.getUserId();
        String searchText = appQueryRequest.getSearchText();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        // 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("appName", searchText).or().like("appDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取应用表封装
     *
     * @param app
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        // 对象转封装类
        AppVO appVO = AppVO.objToVo(app);

        // region 可选
        // 1. 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);
        // endregion

        return appVO;
    }

    /**
     * 获取应用表分页封装
     *
     * @param appPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppVO> appVOList = appList.stream().map(AppVO::objToVo).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        appVOList.forEach(appVO -> {
            Long userId = appVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appVO.setUser(userService.getUserVO(user));
        });
        // endregion

        appVOPage.setRecords(appVOList);
        return appVOPage;
    }


    /**
     * 审核应用（仅管理员)
     *
     * @param reviewRequest
     * @param request
     * @return {@link Boolean}
     */
    @Override
    public Boolean doAppReview(ReviewRequest reviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(reviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = reviewRequest.getId();
        Integer reviewStatus = reviewRequest.getReviewStatus();
        // 审核状态合法校验
        ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
        if (appId != null || reviewStatus != null) {
            ThrowUtils.throwIf(reviewStatusEnum == null, ErrorCode.PARAMS_ERROR);
        }
        // 判断审核应用是否存在
        App oldApp = this.getById(appId);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.PARAMS_ERROR, "审核应用不存在");
        // 当前状态已经是期望修改状态
        ThrowUtils.throwIf(oldApp.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核同一个应用");
        // 更新审核状态
        User loginUser = UserHolder.getUser();
        App app = new App();
        app.setId(appId);
        app.setReviewStatus(reviewStatus);
        app.setReviewMessage(reviewRequest.getReviewMessage());
        app.setReviewerId(loginUser.getId());
        app.setReviewTime(new Date());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 更新缓存-删除旧缓存
        deleteAppInRedis(APP_CACHE);

        return true;
    }

    /**
     * 缓存查询应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return {@link Page}<{@link AppVO}>
     */
    @Override
    public Page<AppVO> listAppVOPageByCacheExpired(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        long current = appQueryRequest.getCurrent();
        long size = appQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        appQueryRequest.setReviewStatus(ReviewStatusEnum.PASSED.getValue());

        // 1. 从缓存中查询应用
        String appCacheKey = APP_CACHE + current + ":" + size;
        String appPageJson = stringRedisTemplate.opsForValue().get(appCacheKey);

        // 2. 判断缓存是否命中
        // 3. 缓存未命中，由当前线程直接建立缓存
        if (StringUtils.isBlank(appPageJson)) {
            // 4.4.1 从数据库查询最新数据
            Page<App> appPageByResource = this.page(new Page<>(current, size),
                    this.getQueryWrapper(appQueryRequest));
            // 4.4.2 更新缓存内容，同时更新逻辑过期时间
            appToRedis(appCacheKey, appPageByResource, APP_CACHE_EXPIRE_HOURS);
            return getAppVOPage(appPageByResource, request);
        }

        // 4. 缓存命中
        RedisData appRedisData = JSONUtil.toBean(appPageJson, RedisData.class);
        JSONObject data = (JSONObject) appRedisData.getData();
        Page<App> appPage = JSON.parseObject(data.toJSONString(2), new TypeReference<Page<App>>() {
        });

        // 4.1 判断缓存是否过期
        LocalDateTime expireTime = appRedisData.getExpireTime();

        // 4.2 未过期，直接返回应用缓存 => 结束
        if (expireTime.isAfter(LocalDateTime.now())) {
            return getAppVOPage(appPage, request);
        }

        // 4.3 缓存过期，尝试获取互斥锁
        String lockKey = LOCK_APP + appCacheKey;
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLock = lock.tryLock();

        // 4.4 获取锁成功，开启独立线程重建缓存
        if (isLock) {
            CompletableFuture.runAsync(() -> {
                try {
                    // 4.4.1 从数据库查询最新数据
                    Page<App> appPageByResource = this.page(new Page<>(current, size),
                            this.getQueryWrapper(appQueryRequest));
                    // 4.4.2 更新缓存内容，同时更新逻辑过期时间
                    appToRedis(appCacheKey, appPageByResource, APP_CACHE_EXPIRE_HOURS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 4.4.3 重建完成释放锁
                    if (lock.isLocked()) {
                        // 只有当前获取锁的线程能释放当前锁
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }

            });
        }

        // 4.5 返回商铺信息
        return getAppVOPage(appPage, request);
    }

    /**
     * APP缓存预热
     *
     * @param appCacheKey
     * @param appPage
     * @param expireSeconds
     */
    public void appToRedis(String appCacheKey, Page<App> appPage, Long expireSeconds) {
        // 封装RedisData
        RedisData redisData = new RedisData();
        redisData.setData(appPage);
        redisData.setExpireTime(LocalDateTime.now().plusHours(expireSeconds));
        // 写入redis
        stringRedisTemplate.opsForValue().set(appCacheKey, JSONUtil.toJsonStr(redisData));
    }


    /**
     * 删除Redis中旧缓存
     *
     * @param appCacheKey
     */
    public void deleteAppInRedis(String appCacheKey) {
        // 获取所有前缀为 APP_CACHE 的 key
        Set<String> keys = stringRedisTemplate.keys(APP_CACHE + "*");

        if (keys != null && !keys.isEmpty()) {
            // 删除这些 key
            stringRedisTemplate.delete(keys);
        }
    }
}
