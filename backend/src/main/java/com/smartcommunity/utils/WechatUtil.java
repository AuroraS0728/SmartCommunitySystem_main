package com.smartcommunity.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class WechatUtil {

    public String exchangeCodeForOpenid(String code) {
        if ("owner-demo".equals(code)) {
            return "openid-owner-1";
        }
        if ("admin-demo".equals(code)) {
            return "openid-admin-1";
        }
        if ("worker-demo".equals(code)) {
            return "openid-worker-1";
        }
        return "mock_openid_" + (code == null ? "unknown" : code);
    }

    public Map<String, Object> mockMiniPay(String outTradeNo, BigDecimal amount) {
        Map<String, Object> pay = new HashMap<>();
        pay.put("appId", "wx_mock_appid");
        pay.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        pay.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        pay.put("package", "prepay_id=mock_" + outTradeNo);
        pay.put("signType", "RSA");
        pay.put("paySign", "MOCK_PAY_SIGN");
        pay.put("amount", amount);
        pay.put("expireAt", LocalDateTime.now().plusMinutes(30).toString());
        return pay;
    }
}
