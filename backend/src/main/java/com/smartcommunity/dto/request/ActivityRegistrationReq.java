package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class ActivityRegistrationReq {
    private String nickname;
    private String phone;
    private Integer age;
    private Integer hasChild;
    private Integer hasPet;
    private String remark;
}
