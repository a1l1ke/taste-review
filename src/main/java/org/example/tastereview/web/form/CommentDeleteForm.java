package org.example.tastereview.web.form;

/** REQ-FUNC-012 댓글 삭제 폼. 비밀번호는 대조 후 버린다. */
public class CommentDeleteForm {

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
