package com.smartcommunity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlogAssetUploadResp {
    private String url;
    private String filename;
}
