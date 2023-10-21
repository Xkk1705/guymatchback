package com.xk.usercenter.enums;



public enum TeamStatusEnum {
    PUBLIC(0, "房间公开"),
    PRIVATE(1,"房间私有"),
    SCRITE(2,"房间加密"),
    ERROR(-1,"参数错误");


    private Integer status;
    private String message;

    public static TeamStatusEnum getEnumByStatus(Integer value) {
        if (value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getStatus() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

//    public static TeamStatusEnum getTeamStatusInstance() {
//        return this;
//    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
