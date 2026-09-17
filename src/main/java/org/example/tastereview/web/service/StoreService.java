package org.example.tastereview.web.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.tastereview.config.AppProperties;
import org.example.tastereview.domain.Store;
import org.example.tastereview.repository.CommentRepository;
import org.example.tastereview.repository.ReviewRepository;
import org.example.tastereview.repository.StoreRepository;
import org.example.tastereview.web.dto.ReviewListItem;
import org.example.tastereview.web.dto.StoreDetailView;
import org.example.tastereview.web.error.NotFoundException;
import org.example.tastereview.web.support.DisplayTime;
import org.example.tastereview.web.support.Normalize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가게 찾기·상세 (REQ-FUNC-002/009).
 * 찾기 정규화: trim+공백 제거+소문자 비교, 이름 오름차순, 최대 search-limit.
 * 평균 별점: HALF_UP 첫째 자리. 리뷰가 없으면 null(화면에 "아직 리뷰가 없습니다").
 */
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ReviewService reviewService;
    private final DisplayTime displayTime;
    private final int searchLimit;
    private final int pageSize;

    public StoreService(StoreRepository storeRepository, ReviewRepository reviewRepository,
                        CommentRepository commentRepository, ReviewService reviewService,
                        DisplayTime displayTime, AppProperties appProperties) {
        this.storeRepository = storeRepository;
        this.reviewRepository = reviewRepository;
        this.commentRepository = commentRepository;
        this.reviewService = reviewService;
        this.displayTime = displayTime;
        this.searchLimit = appProperties.getStore().getSearchLimit();
        this.pageSize = appProperties.getPagination().getPageSize();
    }

    /** 가게 찾기 후보. 검색어가 비었으면 빈 목록. DB 정규화 검색 후 Java에서 최종 판정한다. */
    @Transactional(readOnly = true)
    public List<Store> findCandidates(String storeQuery) {
        if (Normalize.isBlank(storeQuery)) {
            return List.of();
        }
        String wanted = Normalize.comparable(storeQuery);
        return storeRepository
                .searchByNormalizedName(storeQuery.trim(),
                        PageRequest.of(0, searchLimit)).stream()
                .filter(s -> Normalize.comparable(s.getName()).contains(wanted))
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .limit(searchLimit)
                .toList();
    }

    @Transactional(readOnly = true)
    public Store getStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("store.missing"));
    }

    /** 가게 상세: 집계 2쿼리(평균·개수) + 리뷰 페이지 1쿼리 + 댓글 수 집계 1쿼리. */
    @Transactional(readOnly = true)
    public StoreDetailView getDetail(Long id, int page) {
        Store store = getStore(id);
        long reviewCount = reviewRepository.countByStoreId(id);
        String average = null;
        List<ReviewListItem> items = List.of();
        long totalPages = 1;
        if (reviewCount > 0) {
            Double avg = reviewRepository.findAverageRatingByStoreId(id);
            average = formatAverage(avg);
            Page<org.example.tastereview.domain.Review> reviews = reviewRepository
                    .findByStoreIdOrderByCreatedAtDescIdDesc(id,
                            PageRequest.of(Math.max(page, 1) - 1, pageSize));
            totalPages = Math.max(reviews.getTotalPages(), 1);
            List<Long> ids = reviews.map(r -> r.getId()).toList();
            Map<Long, Long> counts = commentRepository.countByReviewIdIn(ids).stream()
                    .collect(Collectors.toMap(
                            c -> c.getReviewId(), c -> c.getCnt(), (a, b) -> a));
            items = reviews.map(r -> reviewService.toListItem(r,
                    counts.getOrDefault(r.getId(), 0L))).toList();
        }
        return new StoreDetailView(store.getId(), store.getName(), store.getAddress(),
                store.getRegion(), store.getCategory(), average, reviewCount, totalPages, items);
    }

    /** 예: 4,5,5 → "4.7". HALF_UP 반올림 후 소수점 첫째 자리 고정 표시. */
    static String formatAverage(Double avg) {
        if (avg == null) {
            return null;
        }
        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }
}
