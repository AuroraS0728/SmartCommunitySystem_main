package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.FaceRegisterReq;
import com.smartcommunity.dto.request.FaceVerifyReq;
import com.smartcommunity.dto.response.FaceVerifyResp;
import com.smartcommunity.service.SeetaFaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
public class FaceController {

    private final SeetaFaceService seetaFaceService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody FaceRegisterReq req) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin can register worker face");
        }
        log.info("Face register request received. workerId={}", req.getWorkerId());
        String status = seetaFaceService.registerFace(req.getWorkerId(), req.getImageBase64());
        return Result.success(Map.of(
                "workerId", req.getWorkerId(),
                "status", status
        ));
    }

    @PostMapping("/verify")
    public Result<FaceVerifyResp> verify(@Valid @RequestBody FaceVerifyReq req) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role == 3 && userId != null && req.getWorkerId() != null && !userId.equals(req.getWorkerId())) {
            return Result.fail(StatusCode.FORBIDDEN, "worker can only verify self face");
        }
        log.info("Face verify request received. role={}, userId={}, workerId={}", role, userId, req.getWorkerId());
        SeetaFaceService.FaceVerifyResult result = seetaFaceService.verifyFaceWithScore(
                req.getWorkerId(), req.getImageBase64()
        );
        FaceVerifyResp resp = new FaceVerifyResp();
        resp.setMatch(result.isMatch());
        resp.setScore(result.getScore());
        resp.setThreshold(result.getThreshold());
        return Result.success(resp);
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam Long workerId) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2 && role != 3) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin or worker can query face status");
        }
        if (role == 3 && userId != null && !userId.equals(workerId)) {
            return Result.fail(StatusCode.FORBIDDEN, "worker can only query self face status");
        }
        log.info("Face status request received. role={}, userId={}, workerId={}", role, userId, workerId);
        boolean registered = seetaFaceService.hasRegisteredFace(workerId);
        return Result.success(Map.of(
                "workerId", workerId,
                "registered", registered
        ));
    }
}
