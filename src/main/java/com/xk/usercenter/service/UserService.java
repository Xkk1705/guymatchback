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


    long userRegister(String userAccount,String password,String checkPassword,String planetCode);

    User userLogin(String userAccount, String password, HttpServletRequest request);

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

    List<User> searchUserByTags(List<String> tagNameList);


}
