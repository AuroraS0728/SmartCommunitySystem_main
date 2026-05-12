package com.smartcommunity.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BlogProfileReq {
    private String siteName;
    private String brand;
    private String heroTitle;
    private String heroTagline;
    private String avatar;
    private String profileName;
    private String profileLine1;
    private String profileLine2;
    private String profileLine3;
    private List<String> skills = new ArrayList<>();
    private List<String> updates = new ArrayList<>();
    private List<String> contacts = new ArrayList<>();
    private List<BlogProjectReq> projects = new ArrayList<>();
    private String footerText;

    @Data
    public static class BlogProjectReq {
        private String title;
        private String description;
        private String imageUrl;
        private String href;
        private List<String> tags = new ArrayList<>();
    }
}
