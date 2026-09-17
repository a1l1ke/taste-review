package org.example.tastereview.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** REQ-FUNC-011 댓글 작성 폼. */
public class CommentForm {

    @NotBlank(message = "{comment.nickname.required}")
    @Size(min = 2, max = 20, message = "{comment.nickname.size}")
    private String nickname;

    @NotBlank(message = "{comment.password.required}")
    @Size(min = 4, max = 20, message = "{comment.password.size}")
    private String password;

    @NotBlank(message = "{comment.content.required}")
    @Size(min = 1, max = 500, message = "{comment.content.size}")
    private String content;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
