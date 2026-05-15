package com.smartcommunity.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BlogNoteReq {
    private String id;
    private String title;
    private String summary;
    private List<String> tags = new ArrayList<>();
    private String markdown;
}
