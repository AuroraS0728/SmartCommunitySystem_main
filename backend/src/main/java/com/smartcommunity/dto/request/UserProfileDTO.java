package com.smartcommunity.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class UserProfileDTO {
    @JsonAlias("has_elderly")
    private Integer hasElderly;

    @JsonAlias("has_child")
    private Integer hasChild;

    @JsonAlias("has_pet")
    private Integer hasPet;

    @JsonAlias("house_area")
    private Integer houseArea;

    @JsonAlias("room_count")
    private Integer roomCount;
}
