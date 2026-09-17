package org.example.tastereview.web.service;

import java.time.Instant;
import java.util.List;
import org.example.tastereview.config.AppProperties;
import org.example.tastereview.domain.Review;
import org.example.tastereview.domain.Store;
import org.example.tastereview.repository.CommentRepository;
import org.example.tastereview.repository.ReviewRepository;
import org.example.tastereview.repository.StoreRepository;
import org.example.tastereview.web.dto.ReviewDetailView;
import org.example.tastereview.web.dto.ReviewListItem;
import org.example.tastereview.web.error.NotFoundException;
import org.example.tastereview.web.form.ReviewCreateForm;
import org.example.tastereview.web.support.DisplayTime;
import org.example.tastereview.web.support.Normalize;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 리뷰 업무 규칙 (REQ-FUNC-004/006/007/008).
 * 가게 중복 처리: 정규화 이름+주소 일치 시 기존 가게 사용, 동시 등록 유니크 충돌 시 재조회.
 * 수정 가능 항목은 title/content/rating이며 수정 시각을 기록한다.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final DisplayTime displayTime;
    private final int storeSearchLimit;

    public ReviewService(ReviewRepository reviewRepository, StoreRepository storeRepository,
                         CommentRepository commentRepository, PasswordEncoder passwordEncoder,
                         DisplayTime displayTime, AppProperties appProperties) {
        this.reviewRepository = reviewRepository;
        this.storeRepository = storeRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.displayTime = displayTime;
        this.storeSearchLimit = appProperties.getStore().getSearchLimit();
    }

    /** 리뷰 등록 후 새 리뷰 id를 반환한다. 작성 비밀번호는 BCrypt 해시로만 저장한다. */
    @Transactional
    public Long create(ReviewCreateForm form) {
        Store store = resolveStore(form);
        Review review = new Review();
        review.setStore(store);
        review.setNickname(form.getNickname());
        review.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        review.setRating(form.getRating());
        review.setTitle(form.getTitle());
        review.setContent(form.getContent());
        review.setCreatedAt(Instant.now());
        return reviewRepository.save(review).getId();
    }

    private Store resolveStore(ReviewCreateForm form) {
        Long storeId = parseStoreId(form.getStoreId());
        if (storeId != null) {
            return storeRepository.findById(storeId)
                    .orElseThrow(() -> new NotFoundException("review.store.selected.missing"));
        }
        String name = form.getStoreName();
        String address = form.getStoreAddress();
        return storeRepository.findByNameAndAddress(name, address)
                .or(() -> findNormalizedStore(name, address))
                .orElseGet(() -> saveStoreOrReuse(form));
    }

    private java.util.Optional<Store> findNormalizedStore(String name, String address) {
        String wantedName = Normalize.comparable(name);
        String wantedAddress = Normalize.comparable(address);
        List<Store> candidates = storeRepository.searchByNormalizedName(name.trim(),
                PageRequest.of(0, storeSearchLimit));
        return candidates.stream()
                .filter(s -> Normalize.comparable(s.getName()).equals(wantedName)
                        && Normalize.comparable(s.getAddress()).equals(wantedAddress))
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .findFirst();
    }

    private Store saveStoreOrReuse(ReviewCreateForm form) {
        Store store = new Store();
        store.setName(form.getStoreName());
        store.setAddress(form.getStoreAddress());
        store.setRegion(form.getStoreRegion());
        store.setCategory(form.getStoreCategory());
        store.setCreatedAt(Instant.now());
        try {
            return storeRepository.save(store);
        } catch (DataIntegrityViolationException e) {
            return storeRepository.findByNameAndAddress(form.getStoreName(), form.getStoreAddress())
                    .or(() -> findNormalizedStore(form.getStoreName(), form.getStoreAddress()))
                    .orElseThrow(() -> e);
        }
    }

    private Long parseStoreId(String storeId) {
        if (Normalize.isBlank(storeId)) {
            return null;
        }
        try {
            return Long.valueOf(storeId.trim());
        } catch (NumberFormatException e) {
            throw new NotFoundException("review.store.selected.missing");
        }
    }

    /** 리뷰+가게 1쿼리, 댓글 목록 1쿼리로 상세 조회한다 (N+1 금지, open-in-view false 가정). */
    @Transactional(readOnly = true)
    public ReviewDetailView getDetail(Long id) {
        Review review = reviewRepository.findWithStoreById(id)
                .orElseThrow(() -> new NotFoundException("review.missing"));
        List<ReviewDetailView.CommentView> comments = commentRepository
                .findByReviewIdOrderByCreatedAtAscIdAsc(id).stream()
                .map(c -> new ReviewDetailView.CommentView(c.getId(), c.getNickname(),
                        c.getContent(), displayTime.format(c.getCreatedAt())))
                .toList();
        Store store = review.getStore();
        return new ReviewDetailView(review.getId(), review.getTitle(), review.getContent(),
                review.getRating(), review.getNickname(),
                displayTime.format(review.getCreatedAt()),
                review.getUpdatedAt() != null, displayTime.format(review.getUpdatedAt()),
                store.getId(), store.getName(), store.getAddress(),
                store.getRegion(), store.getCategory(), comments);
    }

    public enum UpdateResult {
        OK, WRONG_PASSWORD
    }

    /** 비밀번호 일치 시 제목·본문·별점만 수정하고 수정 시각을 기록한다. */
    @Transactional
    public UpdateResult update(Long id, String password, String title, String content, int rating) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("review.missing"));
        if (!passwordEncoder.matches(password == null ? "" : password, review.getPasswordHash())) {
            return UpdateResult.WRONG_PASSWORD;
        }
        review.setTitle(title);
        review.setContent(content);
        review.setRating(rating);
        review.setUpdatedAt(Instant.now());
        return UpdateResult.OK;
    }

    public enum DeleteResult {
        OK, WRONG_PASSWORD
    }

    /** 비밀번호 일치 시 댓글과 함께 영구 삭제한다(ADR-006 하드 삭제). 가게는 남긴다. */
    @Transactional
    public DeleteResult delete(Long id, String password) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("review.missing"));
        if (!passwordEncoder.matches(password == null ? "" : password, review.getPasswordHash())) {
            return DeleteResult.WRONG_PASSWORD;
        }
        commentRepository.deleteByReviewId(id);
        reviewRepository.delete(review);
        return DeleteResult.OK;
    }

    ReviewListItem toListItem(Review review, long commentCount) {
        return new ReviewListItem(review.getId(), review.getTitle(),
                review.getStore().getName(), review.getStore().getCategory(),
                review.getRating(), review.getNickname(),
                displayTime.format(review.getCreatedAt()), commentCount);
    }
}
