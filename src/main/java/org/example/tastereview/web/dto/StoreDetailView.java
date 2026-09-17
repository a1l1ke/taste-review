package org.example.tastereview.web.dto;

import java.util.List;

/** REQ-FUNC-009 가게 상세 화면 모델. 리뷰가 없으면 averageRating이 null이다. */
public class StoreDetailView {

    private final Long id;
    private final String name;
    private final String address;
    private final String region;
    private final String category;
    private final String averageRating;
    private final long reviewCount;
    private final long totalPages;
    private final List<ReviewListItem> reviews;

    public StoreDetailView(Long id, String name, String address, String region, String category,
                           String averageRating, long reviewCount, long totalPages,
                           List<ReviewListItem> reviews) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.region = region;
        this.category = category;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.totalPages = totalPages;
        this.reviews = reviews;
    }

    public boolean hasReviews() {
        return reviewCount > 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getRegion() {
        return region;
    }

    public String getCategory() {
        return category;
    }

    public String getAverageRating() {
        return averageRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public List<ReviewListItem> getReviews() {
        return reviews;
    }
}
