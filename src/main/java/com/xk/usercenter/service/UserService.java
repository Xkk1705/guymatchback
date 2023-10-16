package com.xk.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount
     * @param password
     * @param checkPassword
     * @param planetCode
     * @return
     */
    long userRegister(String userAccount,String password,String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount
     * @param password
     * @param request
     * @return
     */
    User userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param orginUser
     * @return
     */
    User getSafetyUser(User orginUser);

    /**
     * 用户注销
     * @param request
     * @return
     */
    Integer logOut(HttpServletRequest request);

    /**
     * 分页用户信息
     * @param current
     * @param pageSize
     * @return
     */
    BaseResponse<Page> selectUserPage(Long current, Long pageSize);

    /**
     * 更具标签搜索标签交叉的用户
     * @param tagNameList
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * 判断用户是否登录
     * @param request
     * @return
     */
    User isLogin(HttpServletRequest request);


    /**
     * 更新用户信息
     * @param user
     * @param oldUser
     * @return
     */
    BaseResponse<Integer> updateUserMessage(User user, User oldUser);
}
