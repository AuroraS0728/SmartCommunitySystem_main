package com.smartcommunity.service;

import com.smartcommunity.dto.request.AddCommentReq;
import com.smartcommunity.dto.request.PublishForumReq;
import com.smartcommunity.dto.request.PublishLostFoundReq;
import com.smartcommunity.dto.request.PublishSecondHandReq;
import com.smartcommunity.dto.response.PageData;
import com.smartcommunity.entity.ForumComment;
import com.smartcommunity.entity.ForumPost;
import com.smartcommunity.entity.LostFound;
import com.smartcommunity.entity.LostFoundClaim;
import com.smartcommunity.entity.SecondHand;
import com.smartcommunity.entity.SecondHandReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface NeighborService {
    PageData<SecondHand> pageSecondHand(
            Integer page,
            Integer size,
            Integer status,
            String category,
            String keyword,
            String sortBy,
            Boolean mine,
            Long userId,
            Integer role
    );

    Map<String, Object> secondHandDetail(Long id, Long userId);

    SecondHand publishSecondHand(Long userId, PublishSecondHandReq req);

    String uploadSecondHandImage(MultipartFile file);

    SecondHand updateSecondHand(Long id, Long userId, Integer role, PublishSecondHandReq req);

    void deleteSecondHand(Long id, Long userId, Integer role);

    SecondHand updateSecondHandStatus(Long id, Long userId, Integer role, Integer status);

    Map<String, Object> favoriteSecondHand(Long id, Long userId, boolean favorite);

    PageData<SecondHand> pageMyFavoriteSecondHand(Long userId, Integer page, Integer size);

    void reportSecondHand(Long id, Long userId, String reason);

    PageData<SecondHandReport> pageSecondHandReports(Integer page, Integer size, Integer status, Integer role);

    PageData<LostFound> pageLostFound(
            Integer page,
            Integer size,
            Integer type,
            Integer status,
            String keyword,
            Boolean mine,
            Long userId,
            Integer role
    );

    LostFound lostFoundDetail(Long id);

    LostFound publishLostFound(Long userId, PublishLostFoundReq req);

    LostFound updateLostFound(Long id, Long userId, Integer role, PublishLostFoundReq req);

    void deleteLostFound(Long id, Long userId, Integer role);

    LostFound resolveLostFound(Long id, Long userId, Integer role);

    LostFoundClaim claimLostFound(Long id, Long userId, String proof);

    PageData<LostFoundClaim> pageLostFoundClaims(Long lostFoundId, Integer page, Integer size, Long userId, Integer role);

    LostFoundClaim reviewLostFoundClaim(Long claimId, Integer status, Long userId, Integer role);

    PageData<ForumPost> pageForum(
            Integer page,
            Integer size,
            String board,
            String keyword,
            String sortBy,
            Boolean mine,
            Long userId
    );

    Map<String, Object> forumDetail(Long id, Long userId);

    ForumPost publishForum(Long userId, PublishForumReq req);

    ForumPost updateForum(Long id, Long userId, Integer role, PublishForumReq req);

    void deleteForum(Long id, Long userId, Integer role);

    PageData<ForumComment> pageForumComments(Long postId, Integer page, Integer size);

    ForumComment addComment(Long userId, AddCommentReq req);

    void deleteComment(Long id, Long userId, Integer role);

    Map<String, Object> toggleLike(Long id, Long userId);

    ForumPost setTop(Long id, boolean enabled, Integer role);

    ForumPost setEssence(Long id, boolean enabled, Integer role);
}
