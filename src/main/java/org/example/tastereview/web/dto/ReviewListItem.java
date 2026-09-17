package org.example.tastereview.web.dto;

/** 목록 한 항목: 제목(상세 링크), 가게 이름, 별점, 닉네임, 작성일, 댓글 수 (REQ-FUNC-005). */
public class ReviewListItem {

    private final Long id;
    private final String title;
    private final String storeName;
    private final String storeCategory;
    private final int rating;
    private final String nickname;
    private final String createdAt;
    private final long commentCount;

    public ReviewListItem(Long id, String title, String storeName, String storeCategory, int rating,
                          String nickname, String createdAt, long commentCount) {
        this.id = id;
        this.title = title;
        this.storeName = storeName;
        this.storeCategory = storeCategory;
        this.rating = rating;
        this.nickname = nickname;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreCategory() {
        return storeCategory;
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

    public long getCommentCount() {
        return commentCount;
    }
}
