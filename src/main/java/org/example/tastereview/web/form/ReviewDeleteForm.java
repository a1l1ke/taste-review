package org.example.tastereview.web.form;

/** REQ-FUNC-008 리뷰 삭제 폼. 비밀번호는 대조 후 버린다. */
public class ReviewDeleteForm {

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
