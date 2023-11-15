package com.xk.usercenter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.service.UserService;
import com.xk.usercenter.service.impl.UserServiceImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class UserCenterApplicationTests {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private UserServiceImpl userServiceImpl;

    @Test
    void addUserTest() {
        String userAccount = "xukang";
        String password = "12345678";
        String checkPassword = "12345678";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, password, checkPassword,planetCode);
        System.out.println(result);
//        Assert.assertEquals(-1, result);

//        userAccount = "lizihao";
//        password = "123456";
//        checkPassword = "123456";
//        result = userService.userRegister(userAccount, password, checkPassword);
    }

    @Test
    void testPassword() {
        String pwd = "123456";
        // 基于spring框架中的DigestUtils工具类进行密码加密
        String hashedPwd1 = DigestUtils.md5DigestAsHex((pwd).getBytes());
        System.out.println(hashedPwd1);
    }
    @Test
    void testUserList() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> list = userService.list(queryWrapper);
        System.out.println(list);
    }

    @Test
    void testSearchUserByTags() {
        List<String> strings = new ArrayList<>();
        strings.add("student");

//        List<User> users = userService.searchUserByTags(strings);
        List<User> users = userServiceImpl.searchUsersByTags1(strings);
        Assert.assertEquals(users.size(),1);

    }

    @Test
    void testRedisConnect() {
//        stringRedisTemplate.opsForValue().set("test","test");
        int i = 100 % 1000;
        System.out.println(i);
    }

    @Test
    void testSearchUserBytags() {
        List<String> list = new ArrayList<>();
        list.add("篮球");
        List<User> users = userService.searchUserByTags(list);
        System.out.println(users.size());
    }






}
