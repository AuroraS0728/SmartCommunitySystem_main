package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.PublishNoticeReq;
import com.smartcommunity.entity.Notice;
import com.smartcommunity.mapper.NoticeMapper;
import com.smartcommunity.service.LocalCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeMapper noticeMapper;
    private final LocalCacheService localCacheService;

    @GetMapping("/list")
    public Result<List<Notice>> list() {
        List<Notice> rows = localCacheService.getOrLoad("notice:list", Duration.ofSeconds(30), () ->
                noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getIsDeleted, 0)
                        .orderByDesc(Notice::getTop)
                        .orderByDesc(Notice::getPublishTime)));
        return Result.success(rows);
    }

    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        return Result.success(noticeMapper.selectById(id));
    }

    @PostMapping("/publish")
    public Result<Notice> publish(@RequestBody PublishNoticeReq req) {
        Notice n = new Notice();
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());
        n.setTop(req.getTop() == null ? 0 : req.getTop());
        n.setAttachmentUrls(req.getAttachmentUrls());
        n.setPublisher("property-service");
        n.setPublishTime(LocalDateTime.now());
        n.setCreateTime(LocalDateTime.now());
        n.setUpdateTime(LocalDateTime.now());
        n.setIsDeleted(0);
        noticeMapper.insert(n);
        return Result.success(n);
    }

    @PutMapping("/{id}")
    public Result<Notice> update(@PathVariable Long id, @RequestBody PublishNoticeReq req) {
        Notice n = noticeMapper.selectById(id);
        if (n == null) {
            return Result.fail("notice not found");
        }
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());
        n.setTop(req.getTop() == null ? 0 : req.getTop());
        n.setAttachmentUrls(req.getAttachmentUrls());
        n.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(n);
        return Result.success(n);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success("deleted", null);
    }
}
