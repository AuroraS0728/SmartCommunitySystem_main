package com.smartcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcommunity.config.NeighborModuleConfig;
import com.smartcommunity.dto.request.AddCommentReq;
import com.smartcommunity.dto.request.PublishForumReq;
import com.smartcommunity.dto.request.PublishLostFoundReq;
import com.smartcommunity.dto.request.PublishSecondHandReq;
import com.smartcommunity.dto.response.PageData;
import com.smartcommunity.entity.ForumComment;
import com.smartcommunity.entity.ForumPost;
import com.smartcommunity.entity.ForumPostLike;
import com.smartcommunity.entity.LostFound;
import com.smartcommunity.entity.LostFoundClaim;
import com.smartcommunity.entity.SecondHand;
import com.smartcommunity.entity.SecondHandFavorite;
import com.smartcommunity.entity.SecondHandReport;
import com.smartcommunity.mapper.ForumCommentMapper;
import com.smartcommunity.mapper.ForumPostLikeMapper;
import com.smartcommunity.mapper.ForumPostMapper;
import com.smartcommunity.mapper.LostFoundClaimMapper;
import com.smartcommunity.mapper.LostFoundMapper;
import com.smartcommunity.mapper.SecondHandFavoriteMapper;
import com.smartcommunity.mapper.SecondHandMapper;
import com.smartcommunity.mapper.SecondHandReportMapper;
import com.smartcommunity.service.ContentSafetyService;
import com.smartcommunity.service.NeighborService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NeighborServiceImpl implements NeighborService {

    private final NeighborModuleConfig neighborModuleConfig;
    private final ContentSafetyService contentSafetyService;
    private final SecondHandMapper secondHandMapper;
    private final SecondHandFavoriteMapper secondHandFavoriteMapper;
    private final SecondHandReportMapper secondHandReportMapper;
    private final LostFoundMapper lostFoundMapper;
    private final LostFoundClaimMapper lostFoundClaimMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumCommentMapper forumCommentMapper;
    private final ForumPostLikeMapper forumPostLikeMapper;

    @Override
    public PageData<SecondHand> pageSecondHand(Integer page, Integer size, Integer status, String category, String keyword,
                                                String sortBy, Boolean mine, Long userId, Integer role) {
        LambdaQueryWrapper<SecondHand> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(mine) && userId != null) {
            wrapper.eq(SecondHand::getUserId, userId);
        }
        if (status != null) {
            if (!Boolean.TRUE.equals(mine) && !isAdmin(role) && status != 1) {
                throw new IllegalArgumentException("仅可查看在售商品");
            }
            wrapper.eq(SecondHand::getStatus, status);
        } else if (!Boolean.TRUE.equals(mine) && !isAdmin(role)) {
            wrapper.eq(SecondHand::getStatus, 1);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(SecondHand::getCategory, category.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SecondHand::getTitle, kw).or().like(SecondHand::getDescription, kw));
        }
        String sort = normalizeSort(sortBy);
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(SecondHand::getPrice).orderByDesc(SecondHand::getId);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(SecondHand::getPrice).orderByDesc(SecondHand::getId);
        } else if ("hot".equals(sort)) {
            wrapper.orderByDesc(SecondHand::getViewCount).orderByDesc(SecondHand::getId);
        } else {
            wrapper.orderByDesc(SecondHand::getUpdateTime).orderByDesc(SecondHand::getId);
        }
        return pageData(secondHandMapper.selectList(wrapper), page, size);
    }

    @Override
    public Map<String, Object> secondHandDetail(Long id, Long userId) {
        SecondHand item = secondHandMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        secondHandMapper.update(null, new LambdaUpdateWrapper<SecondHand>()
                .eq(SecondHand::getId, id)
                .setSql("view_count = IFNULL(view_count,0) + 1")
                .set(SecondHand::getUpdateTime, LocalDateTime.now()));
        item = secondHandMapper.selectById(id);
        boolean favorited = userId != null && secondHandFavoriteMapper.selectCount(new LambdaQueryWrapper<SecondHandFavorite>()
                .eq(SecondHandFavorite::getUserId, userId)
                .eq(SecondHandFavorite::getSecondHandId, id)) > 0;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("item", item);
        result.put("favorited", favorited);
        result.put("isOwner", userId != null && userId.equals(item.getUserId()));
        return result;
    }

    @Override
    public SecondHand publishSecondHand(Long userId, PublishSecondHandReq req) {
        validateSecondHandReq(req);
        contentSafetyService.validateText(req.getTitle(), req.getDescription());
        SecondHand item = new SecondHand();
        item.setUserId(userId);
        item.setCommunity(blankAsDefault(req.getCommunity(), "Smart Garden"));
        item.setTitle(req.getTitle().trim());
        item.setCategory(blankAsDefault(req.getCategory(), "other"));
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice() == null ? BigDecimal.ZERO : req.getPrice());
        item.setImages(req.getImages());
        item.setContact(req.getContact());
        item.setStatus(1);
        item.setViewCount(0);
        item.setReportCount(0);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        item.setIsDeleted(0);
        secondHandMapper.insert(item);
        return item;
    }

    @Override
    public SecondHand updateSecondHand(Long id, Long userId, Integer role, PublishSecondHandReq req) {
        SecondHand item = secondHandMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        requireOwnerOrAdmin(item.getUserId(), userId, role);
        validateSecondHandReq(req);
        contentSafetyService.validateText(req.getTitle(), req.getDescription());
        item.setCommunity(blankAsDefault(req.getCommunity(), item.getCommunity()));
        item.setTitle(req.getTitle().trim());
        item.setCategory(blankAsDefault(req.getCategory(), item.getCategory()));
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice() == null ? item.getPrice() : req.getPrice());
        item.setImages(req.getImages());
        item.setContact(req.getContact());
        item.setUpdateTime(LocalDateTime.now());
        secondHandMapper.updateById(item);
        return item;
    }

    @Override
    public void deleteSecondHand(Long id, Long userId, Integer role) {
        SecondHand item = secondHandMapper.selectById(id);
        if (item == null) {
            return;
        }
        requireOwnerOrAdmin(item.getUserId(), userId, role);
        secondHandMapper.deleteById(id);
    }

    @Override
    public SecondHand updateSecondHandStatus(Long id, Long userId, Integer role, Integer status) {
        if (status == null || status < 1 || status > 3) {
            throw new IllegalArgumentException("status仅支持1-在售 2-已售 3-下架");
        }
        SecondHand item = secondHandMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        requireOwnerOrAdmin(item.getUserId(), userId, role);
        item.setStatus(status);
        item.setUpdateTime(LocalDateTime.now());
        secondHandMapper.updateById(item);
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> favoriteSecondHand(Long id, Long userId, boolean favorite) {
        SecondHand item = secondHandMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        LambdaQueryWrapper<SecondHandFavorite> query = new LambdaQueryWrapper<SecondHandFavorite>()
                .eq(SecondHandFavorite::getUserId, userId)
                .eq(SecondHandFavorite::getSecondHandId, id);
        SecondHandFavorite existing = secondHandFavoriteMapper.selectOne(query.last("limit 1"));
        if (favorite) {
            if (existing == null) {
                SecondHandFavorite fav = new SecondHandFavorite();
                fav.setUserId(userId);
                fav.setSecondHandId(id);
                fav.setCreateTime(LocalDateTime.now());
                secondHandFavoriteMapper.insert(fav);
            }
        } else if (existing != null) {
            secondHandFavoriteMapper.deleteById(existing.getId());
        }
        long favoriteCount = secondHandFavoriteMapper.selectCount(new LambdaQueryWrapper<SecondHandFavorite>()
                .eq(SecondHandFavorite::getSecondHandId, id));
        Map<String, Object> result = new HashMap<>();
        result.put("secondHandId", id);
        result.put("favorited", favorite);
        result.put("favoriteCount", favoriteCount);
        return result;
    }

    @Override
    public PageData<SecondHand> pageMyFavoriteSecondHand(Long userId, Integer page, Integer size) {
        List<SecondHandFavorite> allFav = secondHandFavoriteMapper.selectList(new LambdaQueryWrapper<SecondHandFavorite>()
                .eq(SecondHandFavorite::getUserId, userId)
                .orderByDesc(SecondHandFavorite::getCreateTime)
                .orderByDesc(SecondHandFavorite::getId));
        int p = normalizePage(page);
        int s = normalizeSize(size);
        int from = (p - 1) * s;
        int to = Math.min(from + s, allFav.size());
        if (from >= allFav.size()) {
            return new PageData<>(List.of(), (long) allFav.size(), p, s);
        }
        List<Long> ids = allFav.subList(from, to).stream()
                .map(SecondHandFavorite::getSecondHandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<SecondHand> items = ids.isEmpty() ? List.of() : secondHandMapper.selectList(new LambdaQueryWrapper<SecondHand>()
                .in(SecondHand::getId, ids));
        Map<Long, SecondHand> itemMap = items.stream().collect(Collectors.toMap(SecondHand::getId, i -> i));
        List<SecondHand> ordered = new ArrayList<>();
        for (Long sid : ids) {
            if (itemMap.containsKey(sid)) {
                ordered.add(itemMap.get(sid));
            }
        }
        return new PageData<>(ordered, (long) allFav.size(), p, s);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportSecondHand(Long id, Long userId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("举报原因不能为空");
        }
        contentSafetyService.validateText(reason);
        SecondHand item = secondHandMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (userId.equals(item.getUserId())) {
            throw new IllegalArgumentException("不能举报自己发布的商品");
        }
        long existing = secondHandReportMapper.selectCount(new LambdaQueryWrapper<SecondHandReport>()
                .eq(SecondHandReport::getSecondHandId, id)
                .eq(SecondHandReport::getUserId, userId)
                .eq(SecondHandReport::getStatus, 0));
        if (existing > 0) {
            throw new IllegalArgumentException("已提交举报，请勿重复提交");
        }
        SecondHandReport report = new SecondHandReport();
        report.setSecondHandId(id);
        report.setUserId(userId);
        report.setReason(reason.trim());
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        report.setIsDeleted(0);
        secondHandReportMapper.insert(report);
        secondHandMapper.update(null, new LambdaUpdateWrapper<SecondHand>()
                .eq(SecondHand::getId, id)
                .setSql("report_count = IFNULL(report_count,0) + 1")
                .set(SecondHand::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public PageData<SecondHandReport> pageSecondHandReports(Integer page, Integer size, Integer status, Integer role) {
        requireAdmin(role);
        LambdaQueryWrapper<SecondHandReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SecondHandReport::getStatus, status);
        }
        wrapper.orderByDesc(SecondHandReport::getCreateTime).orderByDesc(SecondHandReport::getId);
        return pageData(secondHandReportMapper.selectList(wrapper), page, size);
    }

    @Override
    public PageData<LostFound> pageLostFound(Integer page, Integer size, Integer type, Integer status, String keyword,
                                             Boolean mine, Long userId, Integer role) {
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(mine)) {
            wrapper.eq(LostFound::getUserId, userId);
        }
        if (type != null) {
            wrapper.eq(LostFound::getType, type);
        }
        if (status != null) {
            wrapper.eq(LostFound::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(LostFound::getTitle, kw)
                    .or().like(LostFound::getDescription, kw)
                    .or().like(LostFound::getLocation, kw));
        }
        if (!Boolean.TRUE.equals(mine) && !isAdmin(role) && status == null) {
            wrapper.eq(LostFound::getStatus, 1);
        }
        wrapper.orderByDesc(LostFound::getUpdateTime).orderByDesc(LostFound::getId);
        return pageData(lostFoundMapper.selectList(wrapper), page, size);
    }

    @Override
    public LostFound lostFoundDetail(Long id) {
        LostFound row = lostFoundMapper.selectById(id);
        if (row == null) {
            throw new IllegalArgumentException("失物招领信息不存在");
        }
        return row;
    }

    @Override
    public LostFound publishLostFound(Long userId, PublishLostFoundReq req) {
        validateLostFoundReq(req);
        contentSafetyService.validateText(req.getTitle(), req.getDescription(), req.getLocation());
        LostFound row = new LostFound();
        row.setUserId(userId);
        row.setType(req.getType());
        row.setTitle(req.getTitle().trim());
        row.setDescription(req.getDescription().trim());
        row.setLocation(req.getLocation());
        row.setContact(req.getContact());
        row.setImages(req.getImages());
        row.setStatus(1);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        row.setIsDeleted(0);
        lostFoundMapper.insert(row);
        return row;
    }

    @Override
    public LostFound updateLostFound(Long id, Long userId, Integer role, PublishLostFoundReq req) {
        LostFound row = lostFoundMapper.selectById(id);
        if (row == null) {
            throw new IllegalArgumentException("失物招领信息不存在");
        }
        requireOwnerOrAdmin(row.getUserId(), userId, role);
        validateLostFoundReq(req);
        contentSafetyService.validateText(req.getTitle(), req.getDescription(), req.getLocation());
        row.setType(req.getType());
        row.setTitle(req.getTitle().trim());
        row.setDescription(req.getDescription().trim());
        row.setLocation(req.getLocation());
        row.setContact(req.getContact());
        row.setImages(req.getImages());
        row.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(row);
        return row;
    }

    @Override
    public void deleteLostFound(Long id, Long userId, Integer role) {
        LostFound row = lostFoundMapper.selectById(id);
        if (row == null) {
            return;
        }
        requireOwnerOrAdmin(row.getUserId(), userId, role);
        lostFoundMapper.deleteById(id);
    }

    @Override
    public LostFound resolveLostFound(Long id, Long userId, Integer role) {
        LostFound row = lostFoundMapper.selectById(id);
        if (row == null) {
            throw new IllegalArgumentException("失物招领信息不存在");
        }
        requireOwnerOrAdmin(row.getUserId(), userId, role);
        row.setStatus(2);
        row.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(row);
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LostFoundClaim claimLostFound(Long id, Long userId, String proof) {
        if (proof == null || proof.isBlank()) {
            throw new IllegalArgumentException("认领证明不能为空");
        }
        contentSafetyService.validateText(proof);
        LostFound target = lostFoundMapper.selectOne(new LambdaQueryWrapper<LostFound>()
                .eq(LostFound::getId, id)
                .last("for update"));
        if (target == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        if (target.getType() == null || target.getType() != 2) {
            throw new IllegalArgumentException("只有招领信息可认领");
        }
        if (target.getStatus() != null && target.getStatus() != 1) {
            throw new IllegalArgumentException("当前状态不可认领");
        }
        if (userId.equals(target.getUserId())) {
            throw new IllegalArgumentException("不能认领自己发布的信息");
        }
        long existing = lostFoundClaimMapper.selectCount(new LambdaQueryWrapper<LostFoundClaim>()
                .eq(LostFoundClaim::getLostFoundId, id)
                .eq(LostFoundClaim::getUserId, userId)
                .eq(LostFoundClaim::getStatus, 0));
        if (existing > 0) {
            throw new IllegalArgumentException("已提交认领申请，请等待审核");
        }
        LostFoundClaim claim = new LostFoundClaim();
        claim.setLostFoundId(id);
        claim.setUserId(userId);
        claim.setProof(proof.trim());
        claim.setStatus(0);
        claim.setCreateTime(LocalDateTime.now());
        claim.setUpdateTime(LocalDateTime.now());
        claim.setIsDeleted(0);
        lostFoundClaimMapper.insert(claim);
        return claim;
    }

    @Override
    public PageData<LostFoundClaim> pageLostFoundClaims(Long lostFoundId, Integer page, Integer size, Long userId, Integer role) {
        LostFound target = lostFoundMapper.selectById(lostFoundId);
        if (target == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        if (!isAdmin(role) && !Objects.equals(target.getUserId(), userId)) {
            throw new IllegalArgumentException("forbidden");
        }
        List<LostFoundClaim> rows = lostFoundClaimMapper.selectList(new LambdaQueryWrapper<LostFoundClaim>()
                .eq(LostFoundClaim::getLostFoundId, lostFoundId)
                .orderByDesc(LostFoundClaim::getCreateTime)
                .orderByDesc(LostFoundClaim::getId));
        return pageData(rows, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LostFoundClaim reviewLostFoundClaim(Long claimId, Integer status, Long userId, Integer role) {
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("status仅支持1-通过 2-驳回");
        }
        LostFoundClaim claim = lostFoundClaimMapper.selectOne(new LambdaQueryWrapper<LostFoundClaim>()
                .eq(LostFoundClaim::getId, claimId)
                .last("for update"));
        if (claim == null) {
            throw new IllegalArgumentException("认领记录不存在");
        }
        LostFound target = lostFoundMapper.selectById(claim.getLostFoundId());
        if (target == null) {
            throw new IllegalArgumentException("原始记录不存在");
        }
        if (!isAdmin(role) && !Objects.equals(target.getUserId(), userId)) {
            throw new IllegalArgumentException("forbidden");
        }
        if (claim.getStatus() != null && claim.getStatus() != 0) {
            throw new IllegalArgumentException("该认领记录已处理");
        }
        claim.setStatus(status);
        claim.setUpdateTime(LocalDateTime.now());
        lostFoundClaimMapper.updateById(claim);
        if (status == 1) {
            target.setStatus(2);
            target.setUpdateTime(LocalDateTime.now());
            lostFoundMapper.updateById(target);
        }
        return claim;
    }

    @Override
    public PageData<ForumPost> pageForum(Integer page, Integer size, String board, String keyword, String sortBy, Boolean mine, Long userId) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        if (board != null && !board.isBlank()) {
            wrapper.eq(ForumPost::getBoard, board.trim());
        }
        if (Boolean.TRUE.equals(mine)) {
            wrapper.eq(ForumPost::getUserId, userId);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(ForumPost::getTitle, kw).or().like(ForumPost::getContent, kw));
        }
        wrapper.orderByDesc(ForumPost::getIsTop).orderByDesc(ForumPost::getCreateTime).orderByDesc(ForumPost::getId);
        List<ForumPost> rows = forumPostMapper.selectList(wrapper);
        if ("hot".equals(normalizeSort(sortBy))) {
            rows.sort(Comparator
                    .comparing((ForumPost p) -> safeInt(p.getIsTop())).reversed()
                    .thenComparing((ForumPost p) -> safeInt(p.getLikeCnt()) + safeInt(p.getReplyCnt()) + safeInt(p.getViewCount()), Comparator.reverseOrder())
                    .thenComparing(ForumPost::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return pageData(rows, page, size);
    }

    @Override
    public Map<String, Object> forumDetail(Long id, Long userId) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        forumPostMapper.update(null, new LambdaUpdateWrapper<ForumPost>()
                .eq(ForumPost::getId, id)
                .setSql("view_count = IFNULL(view_count,0) + 1")
                .set(ForumPost::getUpdateTime, LocalDateTime.now()));
        post = forumPostMapper.selectById(id);
        boolean liked = userId != null && forumPostLikeMapper.selectCount(new LambdaQueryWrapper<ForumPostLike>()
                .eq(ForumPostLike::getPostId, id)
                .eq(ForumPostLike::getUserId, userId)) > 0;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("post", post);
        result.put("liked", liked);
        result.put("isOwner", userId != null && userId.equals(post.getUserId()));
        return result;
    }

    @Override
    public ForumPost publishForum(Long userId, PublishForumReq req) {
        validateForumReq(req);
        contentSafetyService.validateText(req.getTitle(), req.getContent());
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setBoard(blankAsDefault(req.getBoard(), "chat"));
        post.setTitle(req.getTitle().trim());
        post.setContent(req.getContent().trim());
        post.setViewCount(0);
        post.setLikeCnt(0);
        post.setReplyCnt(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setIsDeleted(0);
        forumPostMapper.insert(post);
        return post;
    }

    @Override
    public ForumPost updateForum(Long id, Long userId, Integer role, PublishForumReq req) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        requireOwnerOrAdmin(post.getUserId(), userId, role);
        validateForumReq(req);
        contentSafetyService.validateText(req.getTitle(), req.getContent());
        post.setBoard(blankAsDefault(req.getBoard(), post.getBoard()));
        post.setTitle(req.getTitle().trim());
        post.setContent(req.getContent().trim());
        post.setUpdateTime(LocalDateTime.now());
        forumPostMapper.updateById(post);
        return post;
    }

    @Override
    public void deleteForum(Long id, Long userId, Integer role) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            return;
        }
        requireOwnerOrAdmin(post.getUserId(), userId, role);
        forumPostMapper.deleteById(id);
    }

    @Override
    public PageData<ForumComment> pageForumComments(Long postId, Integer page, Integer size) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        List<ForumComment> rows = forumCommentMapper.selectList(new LambdaQueryWrapper<ForumComment>()
                .eq(ForumComment::getPostId, postId)
                .orderByAsc(ForumComment::getCreateTime)
                .orderByAsc(ForumComment::getId));
        return pageData(rows, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumComment addComment(Long userId, AddCommentReq req) {
        if (req == null || req.getPostId() == null || req.getPostId() <= 0) {
            throw new IllegalArgumentException("postId无效");
        }
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        contentSafetyService.validateText(req.getContent());
        ForumPost post = forumPostMapper.selectById(req.getPostId());
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        ForumComment comment = new ForumComment();
        comment.setPostId(req.getPostId());
        comment.setUserId(userId);
        comment.setParentId(req.getParentId());
        comment.setContent(req.getContent().trim());
        comment.setLikeCnt(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setIsDeleted(0);
        forumCommentMapper.insert(comment);
        forumPostMapper.update(null, new LambdaUpdateWrapper<ForumPost>()
                .eq(ForumPost::getId, req.getPostId())
                .setSql("reply_cnt = IFNULL(reply_cnt,0) + 1")
                .set(ForumPost::getUpdateTime, LocalDateTime.now()));
        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, Long userId, Integer role) {
        ForumComment comment = forumCommentMapper.selectById(id);
        if (comment == null) {
            return;
        }
        if (!isAdmin(role) && !Objects.equals(comment.getUserId(), userId)) {
            throw new IllegalArgumentException("forbidden");
        }
        forumCommentMapper.deleteById(id);
        forumPostMapper.update(null, new LambdaUpdateWrapper<ForumPost>()
                .eq(ForumPost::getId, comment.getPostId())
                .setSql("reply_cnt = IF(IFNULL(reply_cnt,0) > 0, IFNULL(reply_cnt,0) - 1, 0)")
                .set(ForumPost::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(Long id, Long userId) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        ForumPostLike existing = forumPostLikeMapper.selectOne(new LambdaQueryWrapper<ForumPostLike>()
                .eq(ForumPostLike::getPostId, id)
                .eq(ForumPostLike::getUserId, userId)
                .last("limit 1"));
        boolean liked;
        if (existing == null) {
            ForumPostLike row = new ForumPostLike();
            row.setPostId(id);
            row.setUserId(userId);
            row.setCreateTime(LocalDateTime.now());
            forumPostLikeMapper.insert(row);
            forumPostMapper.update(null, new LambdaUpdateWrapper<ForumPost>()
                    .eq(ForumPost::getId, id)
                    .setSql("like_cnt = IFNULL(like_cnt,0) + 1")
                    .set(ForumPost::getUpdateTime, LocalDateTime.now()));
            liked = true;
        } else {
            forumPostLikeMapper.deleteById(existing.getId());
            forumPostMapper.update(null, new LambdaUpdateWrapper<ForumPost>()
                    .eq(ForumPost::getId, id)
                    .setSql("like_cnt = IF(IFNULL(like_cnt,0) > 0, IFNULL(like_cnt,0) - 1, 0)")
                    .set(ForumPost::getUpdateTime, LocalDateTime.now()));
            liked = false;
        }
        ForumPost latest = forumPostMapper.selectById(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("postId", id);
        result.put("liked", liked);
        result.put("likeCnt", latest == null ? 0 : safeInt(latest.getLikeCnt()));
        return result;
    }

    @Override
    public ForumPost setTop(Long id, boolean enabled, Integer role) {
        requireAdmin(role);
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        post.setIsTop(enabled ? 1 : 0);
        post.setUpdateTime(LocalDateTime.now());
        forumPostMapper.updateById(post);
        return post;
    }

    @Override
    public ForumPost setEssence(Long id, boolean enabled, Integer role) {
        requireAdmin(role);
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        post.setIsEssence(enabled ? 1 : 0);
        post.setUpdateTime(LocalDateTime.now());
        forumPostMapper.updateById(post);
        return post;
    }

    private int normalizePage(Integer page) {
        return page == null || page <= 0 ? 1 : page;
    }

    private int normalizeSize(Integer size) {
        int defaultSize = neighborModuleConfig.getDefaultPageSize() <= 0 ? 10 : neighborModuleConfig.getDefaultPageSize();
        int maxSize = neighborModuleConfig.getMaxPageSize() <= 0 ? 50 : neighborModuleConfig.getMaxPageSize();
        int candidate = (size == null || size <= 0) ? defaultSize : size;
        return Math.min(candidate, maxSize);
    }

    private String normalizeSort(String sortBy) {
        return sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
    }

    private String blankAsDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean isAdmin(Integer role) {
        return role != null && role == 2;
    }

    private void requireAdmin(Integer role) {
        if (!isAdmin(role)) {
            throw new IllegalArgumentException("forbidden");
        }
    }

    private void requireOwnerOrAdmin(Long ownerId, Long userId, Integer role) {
        if (!isAdmin(role) && (ownerId == null || userId == null || !ownerId.equals(userId))) {
            throw new IllegalArgumentException("forbidden");
        }
    }

    private void validateSecondHandReq(PublishSecondHandReq req) {
        if (req == null || req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("title不能为空");
        }
        if (req.getPrice() != null && req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price不能小于0");
        }
    }

    private void validateLostFoundReq(PublishLostFoundReq req) {
        if (req == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (req.getType() == null || (req.getType() != 1 && req.getType() != 2)) {
            throw new IllegalArgumentException("type仅支持1-失物 2-招领");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("title不能为空");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new IllegalArgumentException("description不能为空");
        }
    }

    private void validateForumReq(PublishForumReq req) {
        if (req == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("title不能为空");
        }
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("content不能为空");
        }
    }

    private <T> PageData<T> pageData(List<T> fullList, Integer page, Integer size) {
        List<T> rows = fullList == null ? List.of() : fullList;
        int p = normalizePage(page);
        int s = normalizeSize(size);
        int from = (p - 1) * s;
        int to = Math.min(from + s, rows.size());
        if (from >= rows.size()) {
            return new PageData<>(List.of(), (long) rows.size(), p, s);
        }
        return new PageData<>(rows.subList(from, to), (long) rows.size(), p, s);
    }
}
