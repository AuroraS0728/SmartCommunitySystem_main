package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AnalyzeComplaintReq;
import com.smartcommunity.dto.request.ComplaintReplyReq;
import com.smartcommunity.dto.request.SubmitComplaintReq;
import com.smartcommunity.entity.Complaint;
import com.smartcommunity.mapper.ComplaintMapper;
import com.smartcommunity.service.ComplaintAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintMapper complaintMapper;
    private final ComplaintAnalysisService complaintAnalysisService;

    @PostMapping("/submit")
    public Result<Complaint> submit(@RequestBody SubmitComplaintReq req) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can submit complaint");
        }
        Complaint c = new Complaint();
        c.setUserId(uid);
        c.setType(req.getType());
        c.setTitle(req.getTitle());
        c.setContent(req.getContent());
        c.setImages(req.getImages());
        c.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        complaintAnalysisService.apply(c);
        c.setCreateTime(now);
        c.setUpdateTime(now);
        c.setIsDeleted(0);
        complaintMapper.insert(c);
        return Result.success(c);
    }

    @GetMapping("/list")
    public Result<List<Complaint>> list(@RequestParam(required = false) Integer status,
                                        @RequestParam(required = false) String riskLevel,
                                        @RequestParam(required = false) String sentimentLabel) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1 && role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getIsDeleted, 0);
        if (role == 1) {
            wrapper.eq(Complaint::getUserId, uid);
        }
        if (status != null) {
            wrapper.eq(Complaint::getStatus, status);
        }
        if (StringUtils.hasText(riskLevel)) {
            wrapper.eq(Complaint::getRiskLevel, riskLevel.trim());
        }
        if (StringUtils.hasText(sentimentLabel)) {
            wrapper.eq(Complaint::getSentimentLabel, sentimentLabel.trim());
        }
        wrapper.orderByDesc(Complaint::getId);
        return Result.success(complaintMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Complaint> detail(@PathVariable Long id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null || Integer.valueOf(1).equals(complaint.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "complaint not found");
        }
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1 && role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        if (role == 1 && !uid.equals(complaint.getUserId())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(complaint);
    }

    @PutMapping("/{id}")
    public Result<Complaint> update(@PathVariable Long id, @RequestBody SubmitComplaintReq req) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can update complaint");
        }
        Complaint c = complaintMapper.selectById(id);
        if (c == null || Integer.valueOf(1).equals(c.getIsDeleted())) {
            return Result.fail("complaint not found");
        }
        if (!uid.equals(c.getUserId())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        c.setType(req.getType());
        c.setTitle(req.getTitle());
        c.setContent(req.getContent());
        c.setImages(req.getImages());
        complaintAnalysisService.apply(c);
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.updateById(c);
        return Result.success(c);
    }

    @PostMapping("/{id}/analyze")
    public Result<Complaint> analyze(@PathVariable Long id,
                                     @RequestBody(required = false) AnalyzeComplaintReq req) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin can analyze");
        }
        Complaint c = complaintMapper.selectById(id);
        if (c == null || Integer.valueOf(1).equals(c.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "complaint not found");
        }
        if (req != null && req.getSentimentScore() != null && StringUtils.hasText(req.getSentimentLabel())
                && StringUtils.hasText(req.getRiskLevel())) {
            c.setSentimentScore(req.getSentimentScore());
            c.setSentimentLabel(req.getSentimentLabel().trim());
            c.setRiskLevel(req.getRiskLevel().trim());
            c.setAnalyzedAt(LocalDateTime.now());
        } else {
            complaintAnalysisService.apply(c);
        }
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.updateById(c);
        return Result.success(c);
    }

    @PostMapping("/{id}/reply")
    public Result<Complaint> reply(@PathVariable Long id,
                                   @RequestParam(required = false) String reply,
                                   @RequestBody(required = false) ComplaintReplyReq req) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin can reply");
        }
        Complaint c = complaintMapper.selectById(id);
        if (c == null || Integer.valueOf(1).equals(c.getIsDeleted())) {
            return Result.fail("complaint not found");
        }
        String finalReply = StringUtils.hasText(reply) ? reply.trim() : null;
        if (!StringUtils.hasText(finalReply) && req != null && StringUtils.hasText(req.getReply())) {
            finalReply = req.getReply().trim();
        }
        if (!StringUtils.hasText(finalReply)) {
            return Result.fail(StatusCode.BAD_REQUEST, "reply is empty");
        }
        c.setReply(finalReply);
        c.setReplyTime(LocalDateTime.now());
        c.setStatus(3);
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.updateById(c);
        return Result.success(c);
    }

    @PostMapping("/{id}/satisfaction")
    public Result<Complaint> satisfaction(@PathVariable Long id, @RequestParam Integer satisfaction) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can rate satisfaction");
        }
        Complaint c = complaintMapper.selectById(id);
        if (c == null || Integer.valueOf(1).equals(c.getIsDeleted())) {
            return Result.fail("complaint not found");
        }
        if (!uid.equals(c.getUserId())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        c.setSatisfaction(satisfaction);
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.updateById(c);
        return Result.success(c);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin can delete");
        }
        complaintMapper.deleteById(id);
        return Result.success("deleted", null);
    }
}
