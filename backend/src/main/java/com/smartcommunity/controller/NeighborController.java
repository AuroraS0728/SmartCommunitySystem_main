package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.AddCommentReq;
import com.smartcommunity.dto.request.PublishForumReq;
import com.smartcommunity.dto.request.PublishLostFoundReq;
import com.smartcommunity.dto.request.PublishSecondHandReq;
import com.smartcommunity.entity.ForumComment;
import com.smartcommunity.entity.ForumPost;
import com.smartcommunity.entity.LostFound;
import com.smartcommunity.entity.SecondHand;
import com.smartcommunity.mapper.ForumCommentMapper;
import com.smartcommunity.mapper.ForumPostMapper;
import com.smartcommunity.mapper.LostFoundMapper;
import com.smartcommunity.mapper.SecondHandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/neighbor")
@RequiredArgsConstructor
public class NeighborController {

    private final SecondHandMapper secondHandMapper;
    private final LostFoundMapper lostFoundMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumCommentMapper forumCommentMapper;

    @GetMapping("/second-hand/list")
    public Result<List<SecondHand>> secondHandList(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SecondHand> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SecondHand::getStatus, status);
        }
        wrapper.orderByDesc(SecondHand::getId);
        return Result.success(secondHandMapper.selectList(wrapper));
    }

    @GetMapping("/second-hand/{id}")
    public Result<SecondHand> secondHandDetail(@PathVariable Long id) {
        return Result.success(secondHandMapper.selectById(id));
    }

    @PostMapping("/second-hand/publish")
    public Result<SecondHand> publishSecond(@RequestBody PublishSecondHandReq req) {
        SecondHand v = new SecondHand();
        v.setUserId(AuthContext.getUserId());
        v.setTitle(req.getTitle());
        v.setCategory(req.getCategory());
        v.setPrice(req.getPrice());
        v.setImages(req.getImages());
        v.setContact(req.getContact());
        v.setStatus(1);
        v.setCreateTime(LocalDateTime.now());
        v.setUpdateTime(LocalDateTime.now());
        v.setIsDeleted(0);
        secondHandMapper.insert(v);
        return Result.success(v);
    }

    @PutMapping("/second-hand/{id}")
    public Result<SecondHand> updateSecond(@PathVariable Long id, @RequestBody PublishSecondHandReq req) {
        SecondHand v = secondHandMapper.selectById(id);
        if (v == null) {
            return Result.fail("record not found");
        }
        v.setTitle(req.getTitle());
        v.setCategory(req.getCategory());
        v.setPrice(req.getPrice());
        v.setImages(req.getImages());
        v.setContact(req.getContact());
        v.setUpdateTime(LocalDateTime.now());
        secondHandMapper.updateById(v);
        return Result.success(v);
    }

    @DeleteMapping("/second-hand/{id}")
    public Result<Void> deleteSecond(@PathVariable Long id) {
        secondHandMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    @PostMapping("/second-hand/{id}/sold")
    public Result<SecondHand> markSold(@PathVariable Long id) {
        SecondHand v = secondHandMapper.selectById(id);
        if (v == null) {
            return Result.fail("record not found");
        }
        v.setStatus(2);
        v.setUpdateTime(LocalDateTime.now());
        secondHandMapper.updateById(v);
        return Result.success(v);
    }

    @GetMapping("/lost-found/list")
    public Result<List<LostFound>> lostFoundList(@RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(LostFound::getType, type);
        }
        wrapper.orderByDesc(LostFound::getId);
        return Result.success(lostFoundMapper.selectList(wrapper));
    }

    @GetMapping("/lost-found/{id}")
    public Result<LostFound> lostFoundDetail(@PathVariable Long id) {
        return Result.success(lostFoundMapper.selectById(id));
    }

    @PostMapping("/lost-found/publish")
    public Result<LostFound> publishLostFound(@RequestBody PublishLostFoundReq req) {
        LostFound v = new LostFound();
        v.setUserId(AuthContext.getUserId());
        v.setType(req.getType());
        v.setTitle(req.getTitle());
        v.setDescription(req.getDescription());
        v.setContact(req.getContact());
        v.setImages(req.getImages());
        v.setStatus(1);
        v.setCreateTime(LocalDateTime.now());
        v.setUpdateTime(LocalDateTime.now());
        v.setIsDeleted(0);
        lostFoundMapper.insert(v);
        return Result.success(v);
    }

    @PutMapping("/lost-found/{id}")
    public Result<LostFound> updateLostFound(@PathVariable Long id, @RequestBody PublishLostFoundReq req) {
        LostFound v = lostFoundMapper.selectById(id);
        if (v == null) {
            return Result.fail("record not found");
        }
        v.setType(req.getType());
        v.setTitle(req.getTitle());
        v.setDescription(req.getDescription());
        v.setContact(req.getContact());
        v.setImages(req.getImages());
        v.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(v);
        return Result.success(v);
    }

    @DeleteMapping("/lost-found/{id}")
    public Result<Void> deleteLostFound(@PathVariable Long id) {
        lostFoundMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    @PostMapping("/lost-found/{id}/resolve")
    public Result<LostFound> resolveLostFound(@PathVariable Long id) {
        LostFound v = lostFoundMapper.selectById(id);
        if (v == null) {
            return Result.fail("record not found");
        }
        v.setStatus(2);
        v.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(v);
        return Result.success(v);
    }

    @GetMapping("/forum/list")
    public Result<List<ForumPost>> forumList(@RequestParam(required = false) String board) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        if (board != null && !board.isBlank()) {
            wrapper.eq(ForumPost::getBoard, board);
        }
        wrapper.orderByDesc(ForumPost::getId);
        return Result.success(forumPostMapper.selectList(wrapper));
    }

    @GetMapping("/forum/{id}")
    public Result<ForumPost> forumDetail(@PathVariable Long id) {
        return Result.success(forumPostMapper.selectById(id));
    }

    @GetMapping("/forum/{id}/comments")
    public Result<List<ForumComment>> comments(@PathVariable Long id) {
        return Result.success(forumCommentMapper.selectList(new LambdaQueryWrapper<ForumComment>()
                .eq(ForumComment::getPostId, id)
                .orderByAsc(ForumComment::getId)));
    }

    @PostMapping("/forum/publish")
    public Result<ForumPost> publishForum(@RequestBody PublishForumReq req) {
        ForumPost p = new ForumPost();
        p.setUserId(AuthContext.getUserId());
        p.setBoard(req.getBoard());
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());
        p.setLikeCnt(0);
        p.setReplyCnt(0);
        p.setIsTop(0);
        p.setIsEssence(0);
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        p.setIsDeleted(0);
        forumPostMapper.insert(p);
        return Result.success(p);
    }

    @PutMapping("/forum/{id}")
    public Result<ForumPost> updateForum(@PathVariable Long id, @RequestBody PublishForumReq req) {
        ForumPost p = forumPostMapper.selectById(id);
        if (p == null) {
            return Result.fail("post not found");
        }
        p.setBoard(req.getBoard());
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());
        p.setUpdateTime(LocalDateTime.now());
        forumPostMapper.updateById(p);
        return Result.success(p);
    }

    @DeleteMapping("/forum/{id}")
    public Result<Void> deleteForum(@PathVariable Long id) {
        forumPostMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/forum/comment")
    public Result<ForumComment> addComment(@RequestBody AddCommentReq req) {
        ForumComment c = new ForumComment();
        c.setPostId(req.getPostId());
        c.setUserId(AuthContext.getUserId());
        c.setParentId(req.getParentId());
        c.setContent(req.getContent());
        c.setLikeCnt(0);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        c.setIsDeleted(0);
        forumCommentMapper.insert(c);

        forumPostMapper.update(null, new LambdaUpdateWrapper<ForumPost>()
                .eq(ForumPost::getId, req.getPostId())
                .setSql("reply_cnt = IFNULL(reply_cnt,0) + 1")
                .set(ForumPost::getUpdateTime, LocalDateTime.now()));
        return Result.success(c);
    }

    @PostMapping("/forum/{id}/like")
    public Result<ForumPost> likePost(@PathVariable Long id) {
        forumPostMapper.update(null, new LambdaUpdateWrapper<ForumPost>()
                .eq(ForumPost::getId, id)
                .setSql("like_cnt = IFNULL(like_cnt,0) + 1")
                .set(ForumPost::getUpdateTime, LocalDateTime.now()));
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            return Result.fail("post not found");
        }
        return Result.success(post);
    }
}
