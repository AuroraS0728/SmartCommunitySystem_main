package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.SubmitComplaintReq;
import com.smartcommunity.entity.Complaint;
import com.smartcommunity.mapper.ComplaintMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintMapper complaintMapper;

    @PostMapping("/submit")
    public Result<Complaint> submit(@RequestBody SubmitComplaintReq req) {
        Complaint c = new Complaint();
        c.setUserId(AuthContext.getUserId());
        c.setType(req.getType());
        c.setTitle(req.getTitle());
        c.setContent(req.getContent());
        c.setImages(req.getImages());
        c.setStatus(1);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        c.setIsDeleted(0);
        complaintMapper.insert(c);
        return Result.success(c);
    }

    @GetMapping("/list")
    public Result<List<Complaint>> list(@RequestParam(required = false) Integer status) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if (role != null && role == 1) {
            wrapper.eq(Complaint::getUserId, uid);
        }
        if (status != null) {
            wrapper.eq(Complaint::getStatus, status);
        }
        wrapper.orderByDesc(Complaint::getId);
        return Result.success(complaintMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Complaint> detail(@PathVariable Long id) {
        return Result.success(complaintMapper.selectById(id));
    }

    @PutMapping("/{id}")
    public Result<Complaint> update(@PathVariable Long id, @RequestBody SubmitComplaintReq req) {
        Complaint c = complaintMapper.selectById(id);
        if (c == null) {
            return Result.fail("complaint not found");
        }
        c.setType(req.getType());
        c.setTitle(req.getTitle());
        c.setContent(req.getContent());
        c.setImages(req.getImages());
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.updateById(c);
        return Result.success(c);
    }

    @PostMapping("/{id}/reply")
    public Result<Complaint> reply(@PathVariable Long id, @RequestParam String reply) {
        Complaint c = complaintMapper.selectById(id);
        if (c == null) {
            return Result.fail("complaint not found");
        }
        c.setReply(reply);
        c.setReplyTime(LocalDateTime.now());
        c.setStatus(3);
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.updateById(c);
        return Result.success(c);
    }

    @PostMapping("/{id}/satisfaction")
    public Result<Complaint> satisfaction(@PathVariable Long id, @RequestParam Integer satisfaction) {
        Complaint c = complaintMapper.selectById(id);
        if (c == null) {
            return Result.fail("complaint not found");
        }
        c.setSatisfaction(satisfaction);
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.updateById(c);
        return Result.success(c);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        complaintMapper.deleteById(id);
        return Result.success("deleted", null);
    }
}
