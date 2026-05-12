package com.smartcommunity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssetUploadResp {
    private String url;
    private String filename;
}
