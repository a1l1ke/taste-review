package org.example.tastereview.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** REQ-FUNC-007 리뷰 수정 폼. 수정 가능 항목은 제목·본문·별점, 비밀번호는 대조용(모델에 유지하지 않음). */
public class ReviewEditForm {

    @NotBlank(message = "{review.password.required}")
    @Size(min = 4, max = 20, message = "{review.password.size}")
    private String password;

    @NotBlank(message = "{review.title.required}")
    @Size(min = 1, max = 100, message = "{review.title.size}")
    private String title;

    @NotBlank(message = "{review.content.required}")
    @Size(min = 10, max = 5000, message = "{review.content.size}")
    private String content;

    @NotNull(message = "{review.rating.required}")
    @Min(value = 1, message = "{review.rating.range}")
    @Max(value = 5, message = "{review.rating.range}")
    private Integer rating;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
