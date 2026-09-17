package org.example.tastereview.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** REQ-FUNC-004 리뷰 등록 폼. 길이 제한은 SRS 5절 값과 일치(REQ-NF-013). storeId가 있으면 가게 항목은 무시. */
public class ReviewCreateForm {

    private String storeId;

    @Size(min = 1, max = 50, message = "{review.storeName.size}")
    private String storeName;

    @Size(min = 1, max = 200, message = "{review.storeAddress.size}")
    private String storeAddress;

    @Size(min = 1, max = 30, message = "{review.storeRegion.size}")
    private String storeRegion;

    private String storeCategory;

    @NotBlank(message = "{review.nickname.required}")
    @Size(min = 2, max = 20, message = "{review.nickname.size}")
    private String nickname;

    @NotBlank(message = "{review.password.required}")
    @Size(min = 4, max = 20, message = "{review.password.size}")
    private String password;

    @NotNull(message = "{review.rating.required}")
    @Min(value = 1, message = "{review.rating.range}")
    @Max(value = 5, message = "{review.rating.range}")
    private Integer rating;

    @NotBlank(message = "{review.title.required}")
    @Size(min = 1, max = 100, message = "{review.title.size}")
    private String title;

    @NotBlank(message = "{review.content.required}")
    @Size(min = 10, max = 5000, message = "{review.content.size}")
    private String content;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreRegion() {
        return storeRegion;
    }

    public void setStoreRegion(String storeRegion) {
        this.storeRegion = storeRegion;
    }

    public String getStoreCategory() {
        return storeCategory;
    }

    public void setStoreCategory(String storeCategory) {
        this.storeCategory = storeCategory;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
