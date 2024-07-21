package com.ershi.dahu.service.impl;

import java.util.Date;

import cn.hutool.core.collection.CollUtil;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用表服务实现
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

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
        if (reviewStatus != null){
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
     * 分页获取应用表封装
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
        return true;
    }
}
