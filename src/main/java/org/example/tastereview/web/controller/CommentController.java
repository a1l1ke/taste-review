package org.example.tastereview.web.controller;

import org.example.tastereview.web.dto.ReviewDetailView;
import org.example.tastereview.web.form.CommentDeleteForm;
import org.example.tastereview.web.form.CommentForm;
import org.example.tastereview.web.service.CommentService;
import org.example.tastereview.web.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 댓글 작성·삭제 (REQ-IF-018/019). 처리 후 상세로 리다이렉트(PRG). */
@Controller
public class CommentController {

    private final CommentService commentService;
    private final ReviewService reviewService;
    private final ReviewController reviewController;
    private final WebCommonAdvice common;

    public CommentController(CommentService commentService, ReviewService reviewService,
                             ReviewController reviewController, WebCommonAdvice common) {
        this.commentService = commentService;
        this.reviewService = reviewService;
        this.reviewController = reviewController;
        this.common = common;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class,
                new org.springframework.beans.propertyeditors.StringTrimmerEditor(true));
    }

    /** REQ-IF-018, REQ-FUNC-011: 성공 → 302 상세#comments. 실패 → 200 상세 재표시. */
    @PostMapping("/reviews/{id}/comments")
    public String add(@PathVariable Long id,
                      @Validated @ModelAttribute("commentForm") CommentForm form,
                      BindingResult bindingResult,
                      Model model) {
        if (bindingResult.hasErrors()) {
            ReviewDetailView review = reviewService.getDetail(id);
            reviewController.fillDetail(review, model);
            return "reviews/detail";
        }
        commentService.add(id, form);
        return "redirect:/reviews/" + id + "#comments";
    }

    /** REQ-IF-019, REQ-FUNC-012: 성공·불일치 모두 302 상세#comments. 불일치는 1회 메시지. */
    @PostMapping("/comments/{id}/delete")
    public String delete(@PathVariable Long id,
                         @ModelAttribute("commentDeleteForm") CommentDeleteForm form,
                         RedirectAttributes redirectAttributes) {
        Long reviewId = commentService.findReviewId(id);
        CommentService.DeleteResult result = commentService.delete(id, form.getPassword());
        if (result == CommentService.DeleteResult.WRONG_PASSWORD) {
            redirectAttributes.addFlashAttribute("flashError",
                    common.message("password.mismatch"));
        }
        return "redirect:/reviews/" + reviewId + "#comments";
    }
}
