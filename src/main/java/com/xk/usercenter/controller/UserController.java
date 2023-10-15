package com.xk.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.common.ErrorCode;
import com.xk.usercenter.common.ResultUtil;
import com.xk.usercenter.exception.BusinessException;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.model.request.UserLoginRequest;
import com.xk.usercenter.model.request.UserRegisterRequest;
import com.xk.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xk.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.xk.usercenter.constant.UserConstant.USER_LOGIN_STATUS;


@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {

        if (userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String password = userRegisterRequest.getUserPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, password, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "账户长度不能小于4位");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "密码长度不能小于8位");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "星球编号不能超过5位");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "两次密码不同");
        }
        long userid = userService.userRegister(userAccount, password, checkPassword, planetCode);
        return ResultUtil.success(userid);
    }


    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, password)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (password.length() < 8) {
            return null;
        }
        User user = userService.userLogin(userAccount, password, request);

        return ResultUtil.success(user);
    }

    @PostMapping("/logout")
    public Integer userLoginOut(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        return userService.logOut(request);
    }

    @GetMapping("/search")
    public List<User> searchUser(@PathParam("username") String username, HttpServletRequest request) {
        //鉴权
        if (!isAdmin(request)) {
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> users = userService.list(queryWrapper);
        // 用户脱敏
        return users.stream().map((user -> userService.getSafetyUser(user))).collect(Collectors.toList());

    }

    @GetMapping("/rule")
    public BaseResponse<Page> SearchPageUser(@PathParam("current") Long current, @PathParam("pageSize") Long pageSize, HttpServletRequest request) {
        //鉴权
//        if (!isAdmin(request)) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        }

        // 用户脱敏
        return userService.selectUserPage(current, pageSize);

    }

    @PostMapping("/delete")
    public boolean searchUser(@RequestBody Long id, HttpServletRequest request) {

        if (isAdmin(request)) {
            return false;
        }
        if (id < 0) {
            return false;
        }
        return userService.removeById(id);

    }

    /**
     * 获取当前用户信息
     *
     * @param request re
     * @return User
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            return null;
        }
        // todo 校验用户是否合法
        User user = userService.getById(currentUser.getId());
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtil.success(safetyUser);
    }


    public boolean isAdmin(HttpServletRequest request) {
        //鉴权
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }


}
