package com.smartcommunity.dto.response;

import com.smartcommunity.entity.User;
import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer role;
    private Integer points;
    private Integer creditScore;
    private Integer hasElderly;
    private Integer hasChild;
    private Integer hasPet;
    private Integer houseArea;
    private Integer roomCount;

    public static UserProfileVO from(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setPoints(user.getPoints());
        vo.setCreditScore(user.getCreditScore());
        vo.setHasElderly(user.getHasElderly());
        vo.setHasChild(user.getHasChild());
        vo.setHasPet(user.getHasPet());
        vo.setHouseArea(user.getHouseArea());
        vo.setRoomCount(user.getRoomCount());
        return vo;
    }
}
