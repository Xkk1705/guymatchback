package com.xk.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.xk.usercenter.common.BaseResponse;
import com.xk.usercenter.common.ErrorCode;
import com.xk.usercenter.common.ResultUtil;
import com.xk.usercenter.exception.BusinessException;
import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.service.UserService;
import com.xk.usercenter.mapper.UserMapper;
import com.xk.usercenter.utils.MinDistanceUtils;
import io.swagger.models.auth.In;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import sun.reflect.generics.tree.Tree;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.xk.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.xk.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;


    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public static final String SLAT = "xukang";


    /**
     * 用户注册功能
     *
     * @param userAccount   账号
     * @param password      密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String password, String checkPassword, String planetCode) {
        if (StringUtils.isAnyBlank(userAccount, password, checkPassword)) {
            return -1;
        }
        if (userAccount.length() < 4) {
            return -1;
        }
        if (password.length() < 8) {
            return -1;
        }
        if (planetCode.length() > 5) {
            return -1;
        }
        if (!password.equals(checkPassword)) {
            return -1;
        }
        // 判断账户是否包含特殊字符
        String regEx = "\\pP|\\pS|\\s+";
        Pattern pattern = Pattern.compile(regEx);
        boolean isMatch = pattern.matcher(userAccount).find();
        if (isMatch) {
            return -1;
        }

        //判断用户是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            return -1;
        }

        //判断星球编号是否重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = this.count(queryWrapper);
        if (count > 0) {
            return -1;
        }

        // 基于spring框架中的DigestUtils工具类进行密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SLAT + password).getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        user.setPlanetCode(planetCode);
        this.save(user);
        Long id = user.getId();
        if (id == null) {
            return -1;
        }
        return user.getId();
    }


    /**
     * 用户登录接口
     *
     * @param userAccount 用户账户
     * @param password    用户密码
     * @param request     request
     * @return User（脱敏）
     */
    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, password)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (password.length() < 8) {
            return null;
        }
        // 判断账户是否包含特殊字符
        String regEx = "\\pP|\\pS|\\s+";
        Pattern pattern = Pattern.compile(regEx);
        boolean isMatch = pattern.matcher(userAccount).find();
        if (isMatch) {
            return null;
        }
        //判断账户密码是否一致
        String newPassword = DigestUtils.md5DigestAsHex((SLAT + password).getBytes());

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", newPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            log.info("userAccount cannot match userPassword");
            return null;
        }
        // 用户脱敏
        User safetyUser = this.getSafetyUser(user);
        request.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);
        return safetyUser;
    }


    /**
     * 用户脱敏
     *
     * @param orginUser
     * @return
     */
    @Override
    public User getSafetyUser(User orginUser) {
        User safetyUser = new User();
        safetyUser.setId(orginUser.getId());
        safetyUser.setUsername(orginUser.getUsername());
        safetyUser.setUserAccount(orginUser.getUserAccount());
        safetyUser.setAvatarUrl(orginUser.getAvatarUrl());
        safetyUser.setGender(orginUser.getGender());
        safetyUser.setPhone(orginUser.getPhone());
        safetyUser.setEmail(orginUser.getEmail());
        safetyUser.setUserStatus(orginUser.getUserStatus());
        safetyUser.setCreateTime(orginUser.getCreateTime());
        safetyUser.setPlanetCode(orginUser.getPlanetCode());
        safetyUser.setUserRole(orginUser.getUserRole());
        safetyUser.setTags(orginUser.getTags());
        safetyUser.setProfile(orginUser.getProfile());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public Integer logOut(HttpServletRequest request) {
        if (request == null) {
            return -1;
        }
        request.getSession().removeAttribute(USER_LOGIN_STATUS);
        return 1;
    }

    /**
     * 分页查询用户信息
     *
     * @param current  当前页码
     * @param pageSize 每页数据量
     * @return
     */
    @Override
    public BaseResponse<Page> selectUserPage(Long current, Long pageSize) {
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        if (StringUtils.isNotBlank(username)) {
//            queryWrapper.like("username", username);
//        }
//        List<User> users = userService.list(queryWrapper);
//        // 用户脱敏
//        return users.stream().map((user -> userService.getSafetyUser(user))).collect(Collectors.toList());
        Page<User> userPage = new Page<>(current, pageSize);
        Page<User> page = this.page(userPage);
        return ResultUtil.success(page);
    }

    /**
     * 根据标签查询用户
     *
     * @param tagNameList 标签列表
     * @return user
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : tagNameList) {
            stringBuilder.append(s);
        }
        Gson gson = new Gson();
        Type type1 = new TypeToken<List<User>>() {
        }.getType();
        // 判断是否命中缓存
        String tagsUserKey = "tags:user:key:" + stringBuilder;
        String redisUserTagJson = stringRedisTemplate.opsForValue().get(tagsUserKey);
        if (StringUtils.isNotBlank(redisUserTagJson)) {// 有缓存 不为null ''
            List<User> usrList = gson.fromJson(redisUserTagJson, type1);
            return usrList;
        }
        // 缓存穿透 返回空值
        if ("".equals(redisUserTagJson)) {
            return null;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getUsername, User::getProfile, User::getAvatarUrl, User::getTags);
//        List<User> users = this.list();
        // 限制20便于测试
        List<User> users = this.list(queryWrapper).stream().limit(20).collect(Collectors.toList());


//        List<User> userList = users.stream().filter(user -> {
//            String tagStr = user.getTags();
//            if (StringUtils.isBlank( tagStr)) {
//                return false;
//            }
//            List<String> tagSet = gson.fromJson(tagStr, type);// 获取到每个所拥有的标签
//            for (String tag : tagNameList) {
//                if (!tagSet.contains(tag)) {
//                    return false;// 如果第一个不包含直接就不循环了啊！！！ 所以这里是错的逻辑
//                }
//            }
//            return true;
//        }).map((this::getSafetyUser)).collect(Collectors.toList());
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<User> userList = new ArrayList<>();
        for (User user : users) {
            String tagStr = user.getTags();
            if (StringUtils.isBlank(tagStr)) {
                continue;
            }
            List<String> tempTagList = gson.fromJson(tagStr, type);
            int count = 0;
            for (String s : tempTagList) {
                for (String string : tagNameList) {
                    if (count >= 1) {
                        continue;
                    }
                    if (s.equals(string)) {
                        userList.add(this.getSafetyUser(user));
                        count++;
                    }
                }
            }
        }
        String userTagListJson = gson.toJson(userList);
        stringRedisTemplate.opsForValue().set(tagsUserKey, userTagListJson, 5, TimeUnit.HOURS);
        return userList;
    }

    @Override
    public User isLogin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return currentUser;
    }

    @Override
    public BaseResponse<Integer> updateUserMessage(User user, User oldUser) {
        long id = user.getId();
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 是否为管理员
        if (oldUser.getUserRole() == ADMIN_ROLE) {
            int count = userMapper.updateById(user);
            return ResultUtil.success(count);
        }
        // 是否为当前用户
        if (!Objects.equals(oldUser.getId(), user.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int count = userMapper.updateById(user);
        return ResultUtil.success(count);
    }

    @Override
    public BaseResponse<Page<User>> searchTageUserPage(int currentPage, int pageSize, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long id = user.getId();
        String key = "cache:xk:user:" + id;
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        String userListJson = stringStringValueOperations.get(key);
        Gson gson = new Gson();
        Page<User> userList = gson.fromJson(userListJson, Page.class);
        if (stringStringValueOperations.get(key) != null) {
            return ResultUtil.success(userList);
        }
        Page<User> userPage = new Page<>(currentPage, pageSize);
        userList = this.page(userPage, new QueryWrapper<>());
        String userListJsonRes = gson.toJson(userList);
        stringStringValueOperations.set(key, userListJsonRes, 5, TimeUnit.HOURS);
        return ResultUtil.success(userList);
    }

    @Override
    public List<User> matchUserAndRecommend(int userNum, User loginUser) {
        String tags = loginUser.getTags();
        // 用户没有标签特征 随机返回userNum条数据
        if (tags == null) {
            return this.list().stream().map(this::getSafetyUser).limit(userNum).collect(Collectors.toList());
        }
        // 存出用户匹配分数和下标
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(User::getTags);
        queryWrapper.select(User::getId, User::getTags);
        List<User> userList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(userList)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "没有足够的数据匹配");
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> tagList = gson.fromJson(tags, type);
        List<Pair<Integer, Long>> userPairList = new ArrayList<>();
        // 按照匹配分数升序排序
//        SortedMap<Integer,Long> treeMap = new TreeMap<Integer,Long>((a,b) -> Math.toIntExact(a - b));
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            // 逻辑距离算法匹配相似分数
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, type);
            long score = MinDistanceUtils.minDistanceList(tagList, userTagList);
            // i-> 数组下标  score-> 匹配分数
            userPairList.add(new Pair<>(i, score));
        }
        //按照分数升序排序
        List<Integer> userSortedIndex = userPairList.stream().sorted((a, b) -> Math.toIntExact(a.getValue() - b.getValue()))
                .map(Pair::getKey).limit(userNum).collect(Collectors.toList());
        List<User> sortedUserList = new ArrayList<>();

        for (Integer sortedIndex : userSortedIndex) {
            long id = userList.get(sortedIndex).getId();
            User user = this.getById(id);
            // 脱敏
            User safetyUser = this.getSafetyUser(user);
            sortedUserList.add(safetyUser);
        }
        return sortedUserList;
    }


    public List<User> searchUsersByTags1(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }


}




