package com.xk.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.common.ErrorCode;
import com.xk.usercenter.common.ResultUtil;
import com.xk.usercenter.exception.BusinessException;
import com.xk.usercenter.model.domain.Team;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.model.domain.UserTeam;
import com.xk.usercenter.model.request.TeamQueryRequest;
import com.xk.usercenter.model.request.TeamUpdateRequest;
import com.xk.usercenter.model.vo.TeamVo;
import com.xk.usercenter.model.vo.UserVo;
import com.xk.usercenter.service.TeamService;
import com.xk.usercenter.mapper.TeamMapper;
import com.xk.usercenter.service.UserService;
import com.xk.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.xk.usercenter.constant.TeamContent.*;
import static com.xk.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.xk.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 *
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private TeamMapper teamMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> CreateTeam(Team team, User loginuser) {
        // 判断用户是否登录
        if (loginuser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户没有登录");
        }
        final Long userid = loginuser.getId();
        // 队伍人数不能大于20
        int teamMaxNum = Optional.ofNullable(team.getTeamMaxNum()).orElse(0);
        if (teamMaxNum > 20 || teamMaxNum <= 0) {
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
        // todo
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

    @Override
    public BaseResponse<List<TeamVo>> searchTeamList(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String teamName = teamQueryRequest.getTeamName();
        String description = teamQueryRequest.getDescription();
        Integer teamMaxNum = teamQueryRequest.getTeamMaxNum();
        Date expireTime = teamQueryRequest.getExpireTime();
        Long userid = teamQueryRequest.getUserid();
        Integer teamStatus = teamQueryRequest.getTeamStatus();
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotBlank(teamName), Team::getTeamName, teamName);
        lambdaQueryWrapper.like(StringUtils.isNotBlank(description), Team::getDescription, description);
        lambdaQueryWrapper.eq(teamMaxNum != null && teamMaxNum > 0, Team::getTeamMaxNum, teamMaxNum);
        lambdaQueryWrapper.eq(userid != null && userid > 0, Team::getUserid, userid);
        // 不展示已过期的队伍  where expireTime is null or expireTime > now();
        lambdaQueryWrapper.gt(expireTime != null && expireTime.after(new Date()), Team::getExpireTime, new Date());
        //更具队伍状态查询队伍  如果不是管理员 看不了私密的房间
        if (isAdmin(request)) {
            lambdaQueryWrapper.eq(teamStatus != null && teamStatus > 0, Team::getTeamStatus, teamStatus);
        } else {
            lambdaQueryWrapper.ne(Team::getTeamStatus, 2);
        }
        ArrayList<TeamVo> teamVos = new ArrayList<>();

        List<Team> teamList = this.list(lambdaQueryWrapper);
        // 如果查询为空 返回空数组
        if (CollectionUtils.isEmpty(teamList)) {
            return ResultUtil.success(new ArrayList<>());
        }
        for (Team team : teamList) {
            Long createUserId = team.getUserid();
            if (createUserId == null) {
                continue;
            }
            User createUser = userService.getById(createUserId);
            // 脱敏
            TeamVo teamVo = new TeamVo();
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(team, teamVo);
            BeanUtils.copyProperties(createUser, userVo);
            teamVo.setCreateUser(userVo);
            teamVos.add(teamVo);
        }
        return ResultUtil.success(teamVos);
    }

    @Override
    public BaseResponse<TeamVo> searchTeamListWithTeamUser(Long teamid, HttpServletRequest request) {
        if (teamid == null || teamid < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamid", teamid);
        Team team = this.getById(teamid);
        // 非管理员不可以插叙私密房间
        if (!isAdmin(request)) {
            if (team.getTeamStatus() == 2) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有权限查看私密队伍");
            }
        }
        TeamVo teamVo = new TeamVo();
        BeanUtils.copyProperties(team, teamVo);
        List<UserVo> userList = new ArrayList<>();
        //select *
        //from user_team as ut
        //         inner join team as t on ut.teamid = t.id
        //         inner join user as u on ut.userid = u.id
        //where t.id = ${teamid}
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        if (CollectionUtils.isEmpty(userTeams)) {
            return ResultUtil.success(new TeamVo());
        }
        for (UserTeam userTeam : userTeams) {
            Long userid = userTeam.getUserid();
            if (userid == null) {
                continue;
            }
            UserVo userVo = new UserVo();
            User user = userService.getById(userid);
            BeanUtils.copyProperties(user, userVo);
            userList.add(userVo);
        }
        teamVo.setCount(userTeams.size());
        teamVo.setTeamUserList(userList);
        return ResultUtil.success(teamVo);
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (loginUser == null || loginUser.getId() < 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long teamId = teamUpdateRequest.getId();
        if (teamId == null || teamId < 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没查询到所在队伍");
        }
        // 创建者和管理员才能修改房间信息
        if (team.getUserid() != loginUser.getId() && loginUser.getUserRole() != ADMIN_ROLE) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "没有权限修改房间");
        }
        Integer teamStatus = teamUpdateRequest.getTeamStatus();
        // 修改加密状态 必须设置密码
        if (teamStatus == SECRET) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "请设置加密密码");
            }
        }
        if (teamStatus == PUBLIC) {
            if (StringUtils.isNotBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "公开房间不能设置密码");
            }
        }
        BeanUtils.copyProperties(teamUpdateRequest, team);
        return this.updateById(team);
    }

    @Override
    public BaseResponse<Boolean> joinTeam(Long teamid, String password, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (teamid == null || teamid < 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = this.getById(teamid);
        if (team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该房间已经过期");
        }
        // 不能加入已满的房间
        // todo 加入房间需要加锁 可能多个用户同时查到未满人数
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamid, teamid);
        int joinTeamNum = userTeamService.count(queryWrapper);
        Integer teamMaxNum = team.getTeamMaxNum();
        if (joinTeamNum >= teamMaxNum) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该房间已满");
        }
        // 不是管理员加入不了私密房间
        Integer teamStatus = team.getTeamStatus();
        if (teamStatus == PRIVATE) {
            if (!isAdmin(request)) {
                throw new BusinessException(ErrorCode.NOT_AUTH);
            }
        }
        // 非管理员加入加密房间 必须输入密码
        if (teamStatus == SECRET) {
            if (StringUtils.isBlank(password) || !team.getPassword().equals(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "房间密码不正确");
            }
        }
        LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
        long userid = loginUser.getId();
        // 用户最多加入5个队伍
        wrapper.eq(UserTeam::getUserid, userid);
        List<UserTeam> userTeams = userTeamService.list(wrapper);
        if (userTeams.size() >= 5) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "非VIP用户最多加入5个队伍");
        }
        // 不能重复已加入的房间
        for (UserTeam userTeam : userTeams) {
            if (teamid == userTeam.getTeamid()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入房间");
            }
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserid(userid);
        userTeam.setTeamid(teamid);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        return ResultUtil.success(result);
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> quitTeam(Long teamid, User loginUser) {
        Team team = this.getById(teamid);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有查询到队伍信息");
        }

        long userId = loginUser.getId();
        // 判断是否为队伍中的人
        LambdaQueryWrapper<UserTeam> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(UserTeam::getTeamid, teamid);
        teamLambdaQueryWrapper.eq(UserTeam::getUserid, userId);
        UserTeam isAtTeam = userTeamService.getOne(teamLambdaQueryWrapper);
        if (isAtTeam == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "没有权限");
        }
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        // 队伍只有一个人 解散队伍
        queryWrapper.eq(UserTeam::getTeamid, teamid);
        int count = userTeamService.count(queryWrapper);
        if (count <= 1) {
            boolean result = this.removeById(teamid);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            queryWrapper.eq(UserTeam::getUserid, userId);
            userTeamService.remove(queryWrapper);
            return ResultUtil.success(result);
        } else {
            // 顺位队长位置 limit
            queryWrapper.eq(UserTeam::getTeamid, teamid);
//            queryWrapper.last("order by id limit 2");
            List<UserTeam> userTeams = userTeamService.list(queryWrapper);
            UserTeam nextUserMaster = userTeams.get(1);
            // 移除队员信息  把队长顺位设置
            LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserTeam::getUserid, userId);
            wrapper.eq(UserTeam::getTeamid, teamid);
            boolean result = userTeamService.remove(wrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            team.setUserid(nextUserMaster.getUserid());
            this.updateById(team);
            return ResultUtil.success(result);
        }
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> deleteTeam(Long teamId, User loginUser) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有查询到该队伍信息");
        }
        long userId = loginUser.getId();
        Long masterId = team.getUserid();
        if (masterId != userId) {
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        // 删除关系表
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamid, teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        boolean isDelete = this.removeById(teamId);
        if (!isDelete) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 删除队伍表
        return ResultUtil.success(isDelete);
    }


    public boolean isAdmin(HttpServletRequest request) {
        //鉴权
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}




