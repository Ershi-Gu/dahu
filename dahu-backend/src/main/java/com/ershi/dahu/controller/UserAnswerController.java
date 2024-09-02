package com.ershi.dahu.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ershi.dahu.annotation.AuthCheck;
import com.ershi.dahu.common.BaseResponse;
import com.ershi.dahu.common.DeleteRequest;
import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.common.ResultUtils;
import com.ershi.dahu.constant.UserConstant;
import com.ershi.dahu.exception.BusinessException;
import com.ershi.dahu.exception.ThrowUtils;
import com.ershi.dahu.model.dto.useranswer.UserAnswerAddRequest;
import com.ershi.dahu.model.dto.useranswer.UserAnswerEditRequest;
import com.ershi.dahu.model.dto.useranswer.UserAnswerQueryRequest;
import com.ershi.dahu.model.dto.useranswer.UserAnswerUpdateRequest;
import com.ershi.dahu.model.entity.User;
import com.ershi.dahu.model.entity.UserAnswer;
import com.ershi.dahu.model.vo.UserAnswerVO;
import com.ershi.dahu.scoring.ScoringStrategyExecutor;
import com.ershi.dahu.service.UserAnswerService;
import com.ershi.dahu.service.UserService;
import com.ershi.dahu.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户答题记录表接口
 */
@RestController
@RequestMapping("/useranswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService useranswerService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建用户答题记录表
     *
     * @param useranswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest useranswerAddRequest, HttpServletRequest request) {
        Long userAnswerId = useranswerService.addUserAnswer(useranswerAddRequest, request);
        return ResultUtils.success(userAnswerId);
    }

    /**
     * 删除用户答题记录表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = UserHolder.getUser();
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = useranswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = useranswerService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户答题记录表（仅管理员可用）
     *
     * @param useranswerUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest useranswerUpdateRequest) {
        if (useranswerUpdateRequest == null || useranswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        UserAnswer useranswer = new UserAnswer();
        BeanUtils.copyProperties(useranswerUpdateRequest, useranswer);
        useranswer.setChoices(JSONUtil.toJsonStr(useranswerUpdateRequest.getChoices()));
        // 数据校验
        useranswerService.validUserAnswer(useranswer, false);
        // 判断是否存在
        long id = useranswerUpdateRequest.getId();
        UserAnswer oldUserAnswer = useranswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = useranswerService.updateById(useranswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户答题记录表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserAnswer useranswer = useranswerService.getById(id);
        ThrowUtils.throwIf(useranswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(useranswerService.getUserAnswerVO(useranswer, request));
    }

    /**
     * 分页获取用户答题记录表列表（仅管理员可用）
     *
     * @param useranswerQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest useranswerQueryRequest) {
        long current = useranswerQueryRequest.getCurrent();
        long size = useranswerQueryRequest.getPageSize();
        // 查询数据库
        Page<UserAnswer> useranswerPage = useranswerService.page(new Page<>(current, size),
                useranswerService.getQueryWrapper(useranswerQueryRequest));
        return ResultUtils.success(useranswerPage);
    }

    /**
     * 分页获取用户答题记录表列表（封装类）
     *
     * @param useranswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest useranswerQueryRequest,
                                                                   HttpServletRequest request) {
        long current = useranswerQueryRequest.getCurrent();
        long size = useranswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> useranswerPage = useranswerService.page(new Page<>(current, size),
                useranswerService.getQueryWrapper(useranswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(useranswerService.getUserAnswerVOPage(useranswerPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户答题记录表列表
     *
     * @param useranswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest useranswerQueryRequest,
                                                                     HttpServletRequest request) {
        ThrowUtils.throwIf(useranswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        useranswerQueryRequest.setUserId(loginUser.getId());
        long current = useranswerQueryRequest.getCurrent();
        long size = useranswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> useranswerPage = useranswerService.page(new Page<>(current, size),
                useranswerService.getQueryWrapper(useranswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(useranswerService.getUserAnswerVOPage(useranswerPage, request));
    }

    /**
     * 编辑用户答题记录表（给用户使用）
     *
     * @param useranswerEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest useranswerEditRequest, HttpServletRequest request) {
        if (useranswerEditRequest == null || useranswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserAnswer useranswer = new UserAnswer();
        BeanUtils.copyProperties(useranswerEditRequest, useranswer);
        useranswer.setChoices(JSONUtil.toJsonStr(useranswerEditRequest.getChoices()));
        // 数据校验
        useranswerService.validUserAnswer(useranswer, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = useranswerEditRequest.getId();
        UserAnswer oldUserAnswer = useranswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = useranswerService.updateById(useranswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    /**
     * 返回每次用户进入答题界面后的本次唯一Id
     * @return {@link BaseResponse}<{@link Long}>
     */
    @GetMapping("generate/id")
    public BaseResponse<Long> generateUserAnswerId() {
        return ResultUtils.success(IdUtil.getSnowflakeNextId());
    }
}
