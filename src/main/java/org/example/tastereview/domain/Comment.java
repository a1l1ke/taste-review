package org.example.tastereview.domain;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 댓글 (REQ-DATA-003). 리뷰 삭제 시 DB CASCADE 로 함께 삭제된다 (ADR-006). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comment",
        indexes = @Index(name = "idx_comment_review", columnList = "review_id"))
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_review"))
    private Review review;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Comment(Review review, String nickname, String passwordHash, String content) {
        this.review = review;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.content = content;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
