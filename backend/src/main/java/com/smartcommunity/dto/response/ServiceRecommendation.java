package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class ServiceRecommendation {
    private String serviceId;
    private String serviceName;
    private Integer price;
    private String reason;
    private String imageUrl;
    private String actionPath;
    private Boolean recommended;

    public static ServiceRecommendation of(String serviceId, String serviceName, Integer price, String reason) {
        return of(serviceId, serviceName, price, reason, "", "");
    }

    public static ServiceRecommendation of(String serviceId,
                                           String serviceName,
                                           Integer price,
                                           String reason,
                                           String imageUrl,
                                           String actionPath) {
        ServiceRecommendation item = new ServiceRecommendation();
        item.setServiceId(serviceId);
        item.setServiceName(serviceName);
        item.setPrice(price);
        item.setReason(reason);
        item.setImageUrl(imageUrl);
        item.setActionPath(actionPath);
        item.setRecommended(Boolean.FALSE);
        return item;
    }
}
