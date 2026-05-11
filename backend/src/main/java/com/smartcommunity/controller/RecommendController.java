package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.service.RecommendService;
import com.smartcommunity.service.RecommendRuleConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final RecommendRuleConfigService recommendRuleConfigService;

    @GetMapping("/services")
    public Result<List<ServiceRecommendation>> services() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(recommendService.recommend(userId));
    }

    @GetMapping("/rules")
    public Result<JsonNode> rules() {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can query recommend rules");
        }
        return Result.success(recommendRuleConfigService.getRecommendRules());
    }

    @PutMapping("/rules")
    public Result<JsonNode> saveRules(@RequestBody JsonNode req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can save recommend rules");
        }
        return Result.success(recommendRuleConfigService.saveRecommendRules(req));
    }

    private boolean isAdmin() {
        return RoleUtils.isPropertyAdmin(AuthContext.getRole());
    }
}
