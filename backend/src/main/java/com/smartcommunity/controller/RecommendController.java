package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.RecommendRuleSaveReq;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.entity.RecommendRule;
import com.smartcommunity.service.RecommendRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendRuleService recommendRuleService;

    @GetMapping("/services")
    public Result<List<ServiceRecommendation>> services() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(recommendRuleService.recommend(userId, 3, false));
    }

    @GetMapping("/popup")
    public Result<List<ServiceRecommendation>> popup() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(recommendRuleService.recommend(userId, 2, true));
    }

    @GetMapping("/rules")
    public Result<List<RecommendRule>> rules() {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can query recommend rules");
        }
        return Result.success(recommendRuleService.listRules());
    }

    @PostMapping("/rules")
    public Result<RecommendRule> createRule(@RequestBody RecommendRuleSaveReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can create recommend rules");
        }
        return Result.success(recommendRuleService.createRule(req));
    }

    @PostMapping("/rules/{id}")
    public Result<RecommendRule> updateRule(@PathVariable Long id, @RequestBody RecommendRuleSaveReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can update recommend rules");
        }
        return Result.success(recommendRuleService.updateRule(id, req));
    }

    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can delete recommend rules");
        }
        recommendRuleService.deleteRule(id);
        return Result.success("deleted", null);
    }

    private boolean isAdmin() {
        return RoleUtils.isPropertyAdmin(AuthContext.getRole());
    }
}
