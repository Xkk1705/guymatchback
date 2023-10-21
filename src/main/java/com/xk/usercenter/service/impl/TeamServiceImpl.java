package com.xk.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.common.ErrorCode;
import com.xk.usercenter.common.ResultUtil;
import com.xk.usercenter.exception.BusinessException;
import com.xk.usercenter.model.domain.Team;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.model.domain.UserTeam;
import com.xk.usercenter.service.TeamService;
import com.xk.usercenter.mapper.TeamMapper;
import com.xk.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
 *
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> CreateTeam(Team team, User loginuser) {
        // 判断用户是否登录
        if (loginuser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户没有登录");
        }
        final Long userid = loginuser.getId();
        // 队伍人数不能大于20
        Integer teamMaxNum = Optional.ofNullable(team.getTeamMaxNum()).orElse(0);
        if (teamMaxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不能大于20");
        }
        // 标题长度不能大于20
        String teamName = team.getTeamName();
        String description = team.getDescription();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题长度不能大于20");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不能超过512");
        }
        // 房间是否超时
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "房间超时了2");
        }
        // 房间状态 '队伍状态0-公开 1-私有 2-加密',
        Integer status = Optional.ofNullable(team.getTeamStatus()).orElse(0);
        if (status == 2) {
            // 加密房间必须有密码
            String password = team.getPassword();
            if (StringUtils.isBlank(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请设置加密房间密码");
            }
        }
        // 判断一个用户最多创建5个房间
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.eq("userid", userid);
        int count = this.count(wrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建五个房间");
        }
        // 插入队伍表
        team.setId(null);
        team.setUserid(userid);
        boolean save = this.save(team);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Long teamId = team.getId();
        // 用户队伍插入到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserid(userid);
        userTeam.setTeamid(teamId);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtil.success(teamId);
    }
}




