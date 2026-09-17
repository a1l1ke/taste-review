package org.example.tastereview.web.dto;

import java.util.List;

/** REQ-FUNC-006 리뷰 상세 화면 모델. 시각은 표시용 문자열로만 전달한다. */
public class ReviewDetailView {

    /** 댓글 한 항목. */
    public static class CommentView {
        private final Long id;
        private final String nickname;
        private final String content;
        private final String createdAt;

        public CommentView(Long id, String nickname, String content, String createdAt) {
            this.id = id;
            this.nickname = nickname;
            this.content = content;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public String getNickname() {
            return nickname;
        }

        public String getContent() {
            return content;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    private final Long id;
    private final String title;
    private final String content;
    private final int rating;
    private final String nickname;
    private final String createdAt;
    private final boolean edited;
    private final String updatedAt;
    private final Long storeId;
    private final String storeName;
    private final String storeAddress;
    private final String storeRegion;
    private final String storeCategory;
    private final List<CommentView> comments;

    public ReviewDetailView(Long id, String title, String content, int rating, String nickname,
                            String createdAt, boolean edited, String updatedAt,
                            Long storeId, String storeName, String storeAddress,
                            String storeRegion, String storeCategory,
                            List<CommentView> comments) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.nickname = nickname;
        this.createdAt = createdAt;
        this.edited = edited;
        this.updatedAt = updatedAt;
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storeRegion = storeRegion;
        this.storeCategory = storeCategory;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getRating() {
        return rating;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isEdited() {
        return edited;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public String getStoreRegion() {
        return storeRegion;
    }

    public String getStoreCategory() {
        return storeCategory;
    }

    public List<CommentView> getComments() {
        return comments;
    }
}
