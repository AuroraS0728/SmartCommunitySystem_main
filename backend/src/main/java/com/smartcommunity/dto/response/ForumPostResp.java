package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class ForumPostResp {
    private Long id;
    private String board;
    private String title;
    private Integer likeCnt;
    private Integer replyCnt;
}
