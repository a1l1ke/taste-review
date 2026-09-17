package org.example.tastereview.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.example.tastereview.config.AppProperties;
import org.example.tastereview.domain.Review;
import org.example.tastereview.repository.CommentRepository;
import org.example.tastereview.repository.ReviewRepository;
import org.example.tastereview.web.dto.ReviewListItem;
import org.example.tastereview.web.form.SearchForm;
import org.example.tastereview.web.support.Normalize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REQ-FUNC-010 리뷰 검색. 모든 조건 AND 조합.
 * keyword: 가게 이름·제목·본문 비교용 정규화 부분 일치.
 * region: 가게 지역 비교용 정규화 부분 일치. category: 정확히 일치. minRating: 이상.
 */
@Service
public class SearchService {

    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ReviewService reviewService;
    private final int pageSize;

    public SearchService(ReviewRepository reviewRepository, CommentRepository commentRepository,
                         ReviewService reviewService, AppProperties appProperties) {
        this.reviewRepository = reviewRepository;
        this.commentRepository = commentRepository;
        this.reviewService = reviewService;
        this.pageSize = appProperties.getPagination().getPageSize();
    }

    /** 검색 1쿼리 + 댓글 수 집계 1쿼리. 같은 URL 재요청 시 같은 결과(REQ-FUNC-010 출력). */
    @Transactional(readOnly = true)
    public Page<ReviewListItem> search(SearchForm params) {
        Sort sort = SearchForm.SORT_RATING.equals(params.getSort())
                ? Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id"))
                : Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        Page<Review> page = reviewRepository.findAll(specification(params),
                PageRequest.of(params.getPage() - 1, pageSize, sort));
        List<Long> ids = page.map(Review::getId).toList();
        Map<Long, Long> counts = ids.isEmpty() ? Map.of()
                : commentRepository.countByReviewIdIn(ids).stream()
                        .collect(Collectors.toMap(c -> c.getReviewId(), c -> c.getCnt(), (a, b) -> a));
        return page.map(r -> reviewService.toListItem(r, counts.getOrDefault(r.getId(), 0L)));
    }

    private Specification<Review> specification(SearchForm params) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("store", JoinType.INNER);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (params.hasKeyword()) {
                String keyword = Normalize.comparable(params.getKeyword());
                Predicate onStore = cb.like(normalized(cb, root.get("store").get("name")),
                        like(keyword));
                Predicate onTitle = cb.like(normalized(cb, root.get("title")), like(keyword));
                Predicate onContent = cb.like(normalized(cb, root.get("content")), like(keyword));
                predicates.add(cb.or(onStore, onTitle, onContent));
            }
            if (params.hasRegion()) {
                predicates.add(cb.like(normalized(cb, root.get("store").get("region")),
                        like(Normalize.comparable(params.getRegion()))));
            }
            if (params.hasCategory()) {
                predicates.add(cb.equal(root.get("store").get("category"), params.getCategory()));
            }
            if (params.hasMinRating()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), params.getMinRating()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** 비교용 정규화 근사: 소문자+공백 1칸 제거 후 LIKE. 파라미터는 모든 공백 제거済み. */
    private Expression<String> normalized(jakarta.persistence.criteria.CriteriaBuilder cb,
                                          Expression<String> column) {
        Expression<String> lowered = cb.lower(column);
        return cb.function("replace", String.class, lowered,
                cb.literal(" "), cb.literal(""));
    }

    private String like(String comparable) {
        return "%" + comparable.toLowerCase(Locale.ROOT).replace("%", "").replace("_", "")
                + "%";
    }
}
