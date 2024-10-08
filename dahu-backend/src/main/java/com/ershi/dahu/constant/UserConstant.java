package com.ershi.dahu.constant;

/**
 * 用户常量
 *
 * @author <a href="https://github.com/Ershi-Gu">Ershi-Gu</a>
 * 
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    // region 登录校验常量

    /**
     *用户账号最短位数
     */
    int MIN_USER_ACCOUNT_LENGTH = 4;

    /**
     *用户密码最短位数
     */
    int MIN_USER_PASSWORD_LENGTH = 8;

    /**
     *用户重复密码最短位数
     */
    int MIN_USER_CHECK_PASSWORD_LENGTH = 8;

    // endregion
}
