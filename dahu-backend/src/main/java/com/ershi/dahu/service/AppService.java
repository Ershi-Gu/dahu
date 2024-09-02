package com.ershi.dahu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ershi.dahu.common.BaseResponse;
import com.ershi.dahu.common.ReviewRequest;
import com.ershi.dahu.model.dto.app.AppQueryRequest;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.vo.AppVO;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 应用表服务
 *
 *
 */
public interface AppService extends IService<App> {

    /**
     * 校验数据
     *
     * @param app
     * @param add 对创建的数据进行校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);
    
    /**
     * 获取应用表封装
     *
     * @param app
     * @param request
     * @return
     */
    AppVO getAppVO(App app, HttpServletRequest request);

    /**
     * 分页获取应用表封装
     *
     * @param appPage
     * @param request
     * @return
     */
    Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request);

    /**
     * 审核应用（仅管理员）
     * @param reviewRequest
     * @param request
     * @return {@link Boolean}
     */
    public Boolean doAppReview(ReviewRequest reviewRequest, HttpServletRequest request);


    /**
     * 从缓存中分页查询应用表数据库-逻辑过期实现(封装类)
     * @param appQueryRequest
     * @param request
     * @return {@link Page}<{@link AppVO}>
     */
    Page<AppVO> listAppVOPageByCacheExpired(AppQueryRequest appQueryRequest, HttpServletRequest request);


    /**
     * APP缓存预热
     *
     * @param appCacheKey
     * @param appPage
     * @param expireSeconds
     */
    void appToRedis(String appCacheKey, Page<App> appPage, Long expireSeconds);


    /**
     * 删除Redis中旧缓存
     *
     * @param appCacheKey
     */
    void deleteAppInRedis(String appCacheKey);
}
