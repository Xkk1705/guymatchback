package com.xk.usercenter.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class TeamUpdateRequest {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 密码
     */
    private String password;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 队伍最大人数
     */
    private Integer teamMaxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 队伍状态0-公开 1-私有 3-加密
     */
    private Integer teamStatus;
}
