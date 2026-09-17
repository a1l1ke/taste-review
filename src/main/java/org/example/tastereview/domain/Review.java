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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 리뷰 (REQ-DATA-002). 하드 삭제, 삭제 여부 컬럼 없음 (ADR-006). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "review",
        indexes = {
                @Index(name = "idx_review_store", columnList = "store_id"),
                @Index(name = "idx_review_created", columnList = "created_at")
        })
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_store"))
    private Store store;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    /** 1~5. DB INTEGER + CHECK 제약에 맞춘다 (DEC-001 D5). */
    @Column(nullable = false)
    private Integer rating;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Review(Store store, String nickname, String passwordHash, String title, String content, Integer rating) {
        this.store = store;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.title = title;
        this.content = content;
        this.rating = rating;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
