package org.example.tastereview.repository;

import java.util.Collection;
import java.util.List;
import org.example.tastereview.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 댓글 조회 (REQ-FUNC-006, 011). 작성순(오래된 순) 표시. */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByReviewIdOrderByCreatedAtAscIdAsc(Long reviewId);

    long countByReviewId(Long reviewId);

    void deleteByReviewId(Long reviewId);

    /** 목록 한 페이지의 댓글 수를 한 번의 쿼리로 가져온다 (N+1 방지, REQ-NF-003). */
    @Query("select c.review.id as reviewId, count(c) as cnt from Comment c"
            + " where c.review.id in :reviewIds group by c.review.id")
    List<CommentCount> countByReviewIdIn(@Param("reviewIds") Collection<Long> reviewIds);

    interface CommentCount {
        Long getReviewId();
        Long getCnt();
    }
}
