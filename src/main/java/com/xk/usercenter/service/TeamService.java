package com.xk.usercenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.model.domain.Team;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.model.request.TeamQueryRequest;
import com.xk.usercenter.model.request.TeamUpdateRequest;
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
     * @param teamQueryRequest
     * @param request
     * @return
     */
    BaseResponse<List<TeamVo>> searchTeamList(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    /**
     * 根据teamid查询队伍人员信息 和房间信息
     * @param teamid
     * @param request
     * @return
     */
    BaseResponse<TeamVo>searchTeamListWithTeamUser(Long teamid, HttpServletRequest request);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    BaseResponse<Boolean> joinTeam(Long teamid, String password, HttpServletRequest request);

    /**
     * 退出队伍
     * @param teamid
     * @param loginUser
     * @return
     */
    BaseResponse<Boolean> quitTeam(Long teamid, User loginUser);

    /**
     * 队长解散队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    BaseResponse<Boolean> deleteTeam(Long teamId, User loginUser);
}
