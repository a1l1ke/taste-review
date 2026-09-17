package org.example.tastereview.domain;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 가게 (REQ-DATA-001). 삭제하지 않는다. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "store",
        uniqueConstraints = @UniqueConstraint(name = "uk_store_name_address", columnNames = {"name", "address"}))
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 30)
    private String region;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Store(String name, String address, String region, String category) {
        this.name = name;
        this.address = address;
        this.region = region;
        this.category = category;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
