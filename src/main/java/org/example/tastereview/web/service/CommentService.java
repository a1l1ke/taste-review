package org.example.tastereview.web.service;

import java.time.Instant;
import org.example.tastereview.domain.Comment;
import org.example.tastereview.domain.Review;
import org.example.tastereview.repository.CommentRepository;
import org.example.tastereview.repository.ReviewRepository;
import org.example.tastereview.web.error.NotFoundException;
import org.example.tastereview.web.form.CommentForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 댓글 작성·삭제 (REQ-FUNC-011/012). 수정 기능은 없다. */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public CommentService(CommentRepository commentRepository, ReviewRepository reviewRepository,
                          PasswordEncoder passwordEncoder) {
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 댓글 저장 후 소속 리뷰 id를 반환한다. */
    @Transactional
    public Long add(Long reviewId, CommentForm form) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("review.missing"));
        Comment comment = new Comment();
        comment.setReview(review);
        comment.setNickname(form.getNickname());
        comment.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        comment.setContent(form.getContent());
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        return reviewId;
    }

    public enum DeleteResult {
        OK, WRONG_PASSWORD
    }

    /** 비밀번호 일치 시 영구 삭제한다(ADR-006). 없는 댓글은 404, 불일치는 WRONG_PASSWORD. */
    @Transactional
    public DeleteResult delete(Long commentId, String password) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment.missing"));
        if (!passwordEncoder.matches(password == null ? "" : password, comment.getPasswordHash())) {
            return DeleteResult.WRONG_PASSWORD;
        }
        commentRepository.delete(comment);
        return DeleteResult.OK;
    }

    public Long findReviewId(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment.missing"))
                .getReview().getId();
    }
}
