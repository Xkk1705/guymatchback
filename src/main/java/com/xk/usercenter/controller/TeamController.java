package com.xk.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.common.ErrorCode;
import com.xk.usercenter.common.ResultUtil;
import com.xk.usercenter.exception.BusinessException;
import com.xk.usercenter.model.domain.Team;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.model.request.TeamQuery;
import com.xk.usercenter.service.TeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.xk.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        User loginUser = (User)request.getSession().getAttribute(USER_LOGIN_STATUS);
        return teamService.CreateTeam(team,loginUser);

    }

    @GetMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Long id) {
        if (id < 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        boolean isDelete = teamService.removeById(id);
        if (isDelete) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtil.success(isDelete);

    }

    @GetMapping("/get")
    public BaseResponse<Team> getById(Long id) {
        if (id<0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        return ResultUtil.success(team);

    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        boolean isSave = teamService.save(team);
        if (isSave) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtil.success(isSave);

    }

    @GetMapping("/list")
    public BaseResponse<List<Team>> searchTeamList() {
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        List<Team> list = teamService.list(wrapper);
        return ResultUtil.success(list);

    }
//    @GetMapping("/list1")
//    public BaseResponse<List<Team>> searchTeamListTest(TeamQuery teamQuery) {
//        QueryWrapper<Team> wrapper = new QueryWrapper<>();
//        List<Team> list = teamService.list(wrapper);
//        return ResultUtil.success(list);
//
//    }
//

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> searchTeamPage(@RequestParam("current") Long current, @RequestParam("pageSize") Long pageSize) {
        if (current == null || pageSize == null) {
            current = 1L;
            pageSize = 20L;
        }
        Page<Team> teamPage = new Page<>(current,pageSize);
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        // TODO 分页信息脱敏
        Page<Team> page = teamService.page(teamPage, wrapper);
        return ResultUtil.success(page);

    }

}
