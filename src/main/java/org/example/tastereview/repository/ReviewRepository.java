package org.example.tastereview.repository;

import java.util.Optional;
import org.example.tastereview.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 리뷰 조회·집계 (REQ-FUNC-005, 009, 010, REQ-NF-003). */
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    Page<Review> findByStoreId(Long storeId, Pageable pageable);

    Page<Review> findByStoreIdOrderByCreatedAtDescIdDesc(Long storeId, Pageable pageable);

    /** 상세 조회용: 가게를 함께 fetch 한다 (N+1 방지). */
    @Query("select r from Review r join fetch r.store where r.id = :id")
    Optional<Review> findWithStoreById(@Param("id") Long id);

    long countByStoreId(Long storeId);

    /** 리뷰가 없으면 null 을 반환한다. 평균 표시는 호출 측에서 반올림(HALF_UP, 소수 첫째)한다. */
    @Query("select avg(r.rating) from Review r where r.store.id = :storeId")
    Double findAverageRatingByStoreId(@Param("storeId") Long storeId);
}
