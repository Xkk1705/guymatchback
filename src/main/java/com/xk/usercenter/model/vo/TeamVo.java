package com.xk.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xk.usercenter.model.domain.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamVo {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 队伍最大人数
     */
    private Integer teamMaxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人
     */
    private Long userid;

    /**
     * 队伍人数
     */
    private int count;


    /**
     * 队伍状态0-公开 1-私有 3-加密
     */
    private Integer teamStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private UserVo createUser;

    /**
     * 队伍成员
     */
    private List<UserVo> teamUserList;




}
