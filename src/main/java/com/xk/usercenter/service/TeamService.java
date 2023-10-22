package com.xk.usercenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.model.domain.Team;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.model.request.TeamQuery;
import com.xk.usercenter.model.vo.TeamVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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


    /**
     * 根据条件模糊查询队伍
     * @param teamQuery
     * @param request
     * @return
     */
    BaseResponse<List<TeamVo>> searchTeamList(TeamQuery teamQuery, HttpServletRequest request);

    /**
     * 根据teamid查询队伍人员信息 和房间信息
     * @param teamid
     * @param request
     * @return
     */
    BaseResponse<TeamVo>searchTeamListWithTeamUser(Long teamid, HttpServletRequest request);
}
