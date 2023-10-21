package com.xk.usercenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.model.domain.Team;
import com.xk.usercenter.model.domain.User;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    BaseResponse<Long> CreateTeam(Team team, User loginUser);
}
