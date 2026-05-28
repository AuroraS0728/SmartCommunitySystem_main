package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AddCommentReq;
import com.smartcommunity.dto.request.ForumAdminFlagReq;
import com.smartcommunity.dto.request.LostFoundClaimReq;
import com.smartcommunity.dto.request.LostFoundClaimReviewReq;
import com.smartcommunity.dto.request.PublishForumReq;
import com.smartcommunity.dto.request.PublishLostFoundReq;
import com.smartcommunity.dto.request.PublishSecondHandReq;
import com.smartcommunity.dto.request.SecondHandReportReq;
import com.smartcommunity.dto.response.PageData;
import com.smartcommunity.entity.ForumComment;
import com.smartcommunity.entity.ForumPost;
import com.smartcommunity.entity.LostFound;
import com.smartcommunity.entity.LostFoundClaim;
import com.smartcommunity.entity.SecondHand;
import com.smartcommunity.entity.SecondHandReport;
import com.smartcommunity.service.NeighborService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/neighbor")
@RequiredArgsConstructor
public class NeighborController {

    private final NeighborService neighborService;

    @GetMapping("/second-hand/list")
    public Result<PageData<SecondHand>> secondHandList(@RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String sortBy,
                                                       @RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size,
                                                       @RequestParam(required = false) Boolean mine) {
        return Result.success(neighborService.pageSecondHand(page, size, status, category, keyword, sortBy, mine, AuthContext.getUserId(), AuthContext.getRole()));
    }

    @GetMapping("/second-hand/{id}")
    public Result<Map<String, Object>> secondHandDetail(@PathVariable Long id) {
        return Result.success(neighborService.secondHandDetail(id, AuthContext.getUserId()));
    }

    @PostMapping("/second-hand/publish")
    public Result<SecondHand> publishSecond(@Valid @RequestBody PublishSecondHandReq req) {
        return Result.success(neighborService.publishSecondHand(requireUserId(), req));
    }

    @PostMapping("/second-hand/upload")
    public Result<String> uploadSecondHandImage(@RequestParam("file") MultipartFile file) {
        requireUserId();
        return Result.success(neighborService.uploadSecondHandImage(file));
    }

    @PutMapping("/second-hand/{id}")
    public Result<SecondHand> updateSecond(@PathVariable Long id, @Valid @RequestBody PublishSecondHandReq req) {
        return Result.success(neighborService.updateSecondHand(id, requireUserId(), AuthContext.getRole(), req));
    }

    @DeleteMapping("/second-hand/{id}")
    public Result<Void> deleteSecond(@PathVariable Long id) {
        neighborService.deleteSecondHand(id, requireUserId(), AuthContext.getRole());
        return Result.success("deleted", null);
    }

    @PostMapping("/second-hand/{id}/sold")
    public Result<SecondHand> sold(@PathVariable Long id) { return Result.success(neighborService.updateSecondHandStatus(id, requireUserId(), AuthContext.getRole(), 2)); }
    @PostMapping("/second-hand/{id}/offline")
    public Result<SecondHand> offline(@PathVariable Long id) { return Result.success(neighborService.updateSecondHandStatus(id, requireUserId(), AuthContext.getRole(), 3)); }
    @PostMapping("/second-hand/{id}/favorite")
    public Result<Map<String, Object>> favorite(@PathVariable Long id) { return Result.success(neighborService.favoriteSecondHand(id, requireUserId(), true)); }
    @DeleteMapping("/second-hand/{id}/favorite")
    public Result<Map<String, Object>> unfavorite(@PathVariable Long id) { return Result.success(neighborService.favoriteSecondHand(id, requireUserId(), false)); }

    @GetMapping("/second-hand/favorite/list")
    public Result<PageData<SecondHand>> myFavorites(@RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer size) {
        return Result.success(neighborService.pageMyFavoriteSecondHand(requireUserId(), page, size));
    }

    @PostMapping("/second-hand/{id}/report")
    public Result<Void> report(@PathVariable Long id, @Valid @RequestBody SecondHandReportReq req) {
        neighborService.reportSecondHand(id, requireUserId(), req.getReason());
        return Result.success("reported", null);
    }

    @GetMapping("/second-hand/report/list")
    public Result<PageData<SecondHandReport>> reportList(@RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size,
                                                         @RequestParam(required = false) Integer status) {
        if (!isAdmin()) return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        return Result.success(neighborService.pageSecondHandReports(page, size, status, AuthContext.getRole()));
    }

    @GetMapping("/lost-found/list")
    public Result<PageData<LostFound>> lostFoundList(@RequestParam(required = false) Integer type,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size,
                                                     @RequestParam(required = false) Boolean mine) {
        return Result.success(neighborService.pageLostFound(page, size, type, status, keyword, mine, AuthContext.getUserId(), AuthContext.getRole()));
    }

    @GetMapping("/lost-found/{id}")
    public Result<LostFound> lostFoundDetail(@PathVariable Long id) { return Result.success(neighborService.lostFoundDetail(id)); }
    @PostMapping("/lost-found/publish")
    public Result<LostFound> publishLostFound(@Valid @RequestBody PublishLostFoundReq req) { return Result.success(neighborService.publishLostFound(requireUserId(), req)); }
    @PutMapping("/lost-found/{id}")
    public Result<LostFound> updateLostFound(@PathVariable Long id, @Valid @RequestBody PublishLostFoundReq req) { return Result.success(neighborService.updateLostFound(id, requireUserId(), AuthContext.getRole(), req)); }
    @DeleteMapping("/lost-found/{id}")
    public Result<Void> deleteLostFound(@PathVariable Long id) { neighborService.deleteLostFound(id, requireUserId(), AuthContext.getRole()); return Result.success("deleted", null); }
    @PostMapping("/lost-found/{id}/resolve")
    public Result<LostFound> resolveLostFound(@PathVariable Long id) { return Result.success(neighborService.resolveLostFound(id, requireUserId(), AuthContext.getRole())); }
    @PostMapping("/lost-found/{id}/claim")
    public Result<LostFoundClaim> claimLostFound(@PathVariable Long id, @Valid @RequestBody LostFoundClaimReq req) { return Result.success(neighborService.claimLostFound(id, requireUserId(), req.getProof())); }
    @GetMapping("/lost-found/{id}/claims")
    public Result<PageData<LostFoundClaim>> lostFoundClaims(@PathVariable Long id, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return Result.success(neighborService.pageLostFoundClaims(id, page, size, requireUserId(), AuthContext.getRole())); }
    @PostMapping("/lost-found/claim/{claimId}/review")
    public Result<LostFoundClaim> reviewClaim(@PathVariable Long claimId, @Valid @RequestBody LostFoundClaimReviewReq req) { return Result.success(neighborService.reviewLostFoundClaim(claimId, req.getStatus(), requireUserId(), AuthContext.getRole())); }

    @GetMapping("/forum/list")
    public Result<PageData<ForumPost>> forumList(@RequestParam(required = false) String board,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String sortBy,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size,
                                                 @RequestParam(required = false) Boolean mine) {
        return Result.success(neighborService.pageForum(page, size, board, keyword, sortBy, mine, AuthContext.getUserId()));
    }

    @GetMapping("/forum/{id}")
    public Result<Map<String, Object>> forumDetail(@PathVariable Long id) { return Result.success(neighborService.forumDetail(id, AuthContext.getUserId())); }
    @GetMapping("/forum/{id}/comments")
    public Result<PageData<ForumComment>> comments(@PathVariable Long id, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return Result.success(neighborService.pageForumComments(id, page, size)); }
    @PostMapping("/forum/publish")
    public Result<ForumPost> publishForum(@Valid @RequestBody PublishForumReq req) { return Result.success(neighborService.publishForum(requireUserId(), req)); }
    @PutMapping("/forum/{id}")
    public Result<ForumPost> updateForum(@PathVariable Long id, @Valid @RequestBody PublishForumReq req) { return Result.success(neighborService.updateForum(id, requireUserId(), AuthContext.getRole(), req)); }
    @DeleteMapping("/forum/{id}")
    public Result<Void> deleteForum(@PathVariable Long id) { neighborService.deleteForum(id, requireUserId(), AuthContext.getRole()); return Result.success("deleted", null); }
    @PostMapping("/forum/comment")
    public Result<ForumComment> addComment(@Valid @RequestBody AddCommentReq req) { return Result.success(neighborService.addComment(requireUserId(), req)); }
    @DeleteMapping("/forum/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) { neighborService.deleteComment(id, requireUserId(), AuthContext.getRole()); return Result.success("deleted", null); }
    @PostMapping("/forum/{id}/like")
    public Result<Map<String, Object>> likeToggle(@PathVariable Long id) { return Result.success(neighborService.toggleLike(id, requireUserId())); }
    @PostMapping("/forum/{id}/top")
    public Result<ForumPost> setTop(@PathVariable Long id, @Valid @RequestBody ForumAdminFlagReq req) { if (!isAdmin()) return Result.fail(StatusCode.FORBIDDEN, "forbidden"); return Result.success(neighborService.setTop(id, req.getEnabled(), AuthContext.getRole())); }
    @PostMapping("/forum/{id}/essence")
    public Result<ForumPost> setEssence(@PathVariable Long id, @Valid @RequestBody ForumAdminFlagReq req) { if (!isAdmin()) return Result.fail(StatusCode.FORBIDDEN, "forbidden"); return Result.success(neighborService.setEssence(id, req.getEnabled(), AuthContext.getRole())); }

    private Long requireUserId() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("unauthorized");
        }
        return userId;
    }

    private boolean isAdmin() {
        Integer role = AuthContext.getRole();
        return role != null && role == 2;
    }
}
