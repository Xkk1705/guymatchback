package com.xk.usercenter.model.request;

import lombok.Data;

/**
 * 加入队伍请求参数
 */
@Data
public class JoinTeamRequest {
    private Long teamId;
    private String password;
}
