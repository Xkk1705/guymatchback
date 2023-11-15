package com.xk.usercenter.model.vo;


import lombok.Data;

@Data
public class UserVo {
    /**
     * 主键id
     */
    private long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态  0-正常
     */
    private Integer userStatus;

    /**
     * 用户权限 0-普通用户 1-管理员用户
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;


}
