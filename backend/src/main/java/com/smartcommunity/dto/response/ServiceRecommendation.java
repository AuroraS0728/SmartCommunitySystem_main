package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class ServiceRecommendation {
    private String serviceId;
    private String serviceName;
    private Integer price;
    private String reason;

    public static ServiceRecommendation of(String serviceId, String serviceName, Integer price, String reason) {
        ServiceRecommendation item = new ServiceRecommendation();
        item.setServiceId(serviceId);
        item.setServiceName(serviceName);
        item.setPrice(price);
        item.setReason(reason);
        return item;
    }
}
