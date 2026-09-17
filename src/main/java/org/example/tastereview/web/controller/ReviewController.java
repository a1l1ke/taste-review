package org.example.tastereview.web.controller;

import java.util.List;
import org.example.tastereview.domain.Store;
import org.example.tastereview.web.dto.PageWindow;
import org.example.tastereview.web.dto.ReviewDetailView;
import org.example.tastereview.web.dto.ReviewListItem;
import org.example.tastereview.web.error.NotFoundException;
import org.example.tastereview.web.form.CommentForm;
import org.example.tastereview.web.form.ReviewCreateForm;
import org.example.tastereview.web.form.ReviewDeleteForm;
import org.example.tastereview.web.form.ReviewEditForm;
import org.example.tastereview.web.form.SearchForm;
import org.example.tastereview.web.service.ReviewService;
import org.example.tastereview.web.service.SearchService;
import org.example.tastereview.web.service.StoreService;
import org.example.tastereview.web.support.Normalize;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 리뷰 목록·작성·상세·수정·삭제 (REQ-IF-010~017).
 * 검증 실패 → 200 폼 재표시+입력 유지(비밀번호 제외). 성공 → PRG 302.
 */
@Controller
public class ReviewController {

    private final SearchService searchService;
    private final ReviewService reviewService;
    private final StoreService storeService;
    private final WebCommonAdvice common;

    public ReviewController(SearchService searchService, ReviewService reviewService,
                            StoreService storeService, WebCommonAdvice common) {
        this.searchService = searchService;
        this.reviewService = reviewService;
        this.storeService = storeService;
        this.common = common;
    }

    /** trim 정규화 후 검증되도록 바인딩 시점에 공백을 제거한다. */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class,
                new org.springframework.beans.propertyeditors.StringTrimmerEditor(true));
    }

    /** REQ-IF-010/011: 홈과 목록은 같은 화면. 검색 조건은 URL에 유지된다. */
    @GetMapping({"/", "/reviews"})
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String region,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String minRating,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) String page,
                       Model model) {
        SearchForm search = SearchForm.sanitize(keyword, region, category, minRating, sort, page,
                common.getCategories());
        Page<ReviewListItem> results = searchService.search(search);
        model.addAttribute("search", search);
        model.addAttribute("results", results);
        model.addAttribute("pageWindow",
                PageWindow.of(search.getPage(), results.getTotalPages()));
        model.addAttribute("baseQuery", search.toQueryString());
        model.addAttribute("pageTitle", common.message("review.list.title"));
        model.addAttribute("pageDescription", common.getSiteDescription());
        return "reviews/list";
    }

    /** REQ-IF-012, REQ-FUNC-001~003: 작성 폼 + 가게 찾기 + 가게 선택. */
    @GetMapping("/reviews/new")
    public String newForm(@RequestParam(required = false) String storeQuery,
                          @RequestParam(required = false) String storeId,
                          Model model) {
        if (!model.containsAttribute("form")) {
            ReviewCreateForm form = new ReviewCreateForm();
            form.setStoreId(storeId);
            model.addAttribute("form", form);
        }
        fillStoreSelection(storeQuery, storeId, model);
        model.addAttribute("pageTitle", common.message("review.form.title"));
        model.addAttribute("pageDescription", common.getSiteDescription());
        return "reviews/new";
    }

    private void fillStoreSelection(String storeQuery, String storeId, Model model) {
        List<Store> candidates = storeService.findCandidates(storeQuery);
        model.addAttribute("storeQuery", storeQuery == null ? "" : storeQuery);
        model.addAttribute("candidates", candidates);
        model.addAttribute("searched", !Normalize.isBlank(storeQuery));
        if (!Normalize.isBlank(storeId)) {
            try {
                model.addAttribute("selectedStore",
                        storeService.getStore(Long.valueOf(storeId.trim())));
            } catch (NumberFormatException | NotFoundException e) {
                model.addAttribute("storeMissing", true);
            }
        }
    }

    /** REQ-IF-013, REQ-FUNC-004: 등록 성공 → 302 상세. 실패 → 200 폼 재표시. */
    @PostMapping("/reviews")
    public String create(@Validated @ModelAttribute("form") ReviewCreateForm form,
                         BindingResult bindingResult,
                         @RequestParam(required = false) String storeQuery,
                         Model model) {
        validateStoreFields(form, bindingResult);
        if (bindingResult.hasErrors()) {
            fillStoreSelection(storeQuery, form.getStoreId(), model);
            model.addAttribute("pageTitle", common.message("review.form.title"));
            model.addAttribute("pageDescription", common.getSiteDescription());
            return "reviews/new";
        }
        try {
            Long id = reviewService.create(form);
            return "redirect:/reviews/" + id;
        } catch (NotFoundException e) {
            bindingResult.rejectValue("storeId", "error", common.message(e.getMessage()));
            fillStoreSelection(storeQuery, form.getStoreId(), model);
            model.addAttribute("pageTitle", common.message("review.form.title"));
            model.addAttribute("pageDescription", common.getSiteDescription());
            return "reviews/new";
        }
    }

    private void validateStoreFields(ReviewCreateForm form, BindingResult bindingResult) {
        if (!Normalize.isBlank(form.getStoreId())) {
            return;
        }
        if (Normalize.isBlank(form.getStoreName())) {
            bindingResult.rejectValue("storeName", "error",
                    common.message("review.form.storeName.required"));
        }
        if (Normalize.isBlank(form.getStoreAddress())) {
            bindingResult.rejectValue("storeAddress", "error",
                    common.message("review.form.storeAddress.required"));
        }
        if (Normalize.isBlank(form.getStoreRegion())) {
            bindingResult.rejectValue("storeRegion", "error",
                    common.message("review.form.storeRegion.required"));
        }
        if (Normalize.isBlank(form.getStoreCategory())
                || !common.isAllowedCategory(form.getStoreCategory())) {
            bindingResult.rejectValue("storeCategory", "error",
                    common.message("review.storeCategory.invalid"));
        }
    }

    /** REQ-IF-014, REQ-FUNC-006: 상세 + 댓글 목록·작성폼 + 삭제폼 + 수정 링크. */
    @GetMapping("/reviews/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ReviewDetailView review = reviewService.getDetail(id);
        fillDetail(review, model);
        return "reviews/detail";
    }

    void fillDetail(ReviewDetailView review, Model model) {
        model.addAttribute("review", review);
        if (!model.containsAttribute("commentForm")) {
            model.addAttribute("commentForm", new CommentForm());
        }
        model.addAttribute("reviewDeleteForm", new ReviewDeleteForm());
        model.addAttribute("commentDeleteForm", new org.example.tastereview.web.form.CommentDeleteForm());
        model.addAttribute("pageTitle", review.getTitle() + " - " + review.getStoreName()
                + " | " + common.getSiteName());
        model.addAttribute("pageDescription", review.getTitle() + " - " + review.getStoreName());
    }

    /** REQ-IF-015: 현재 제목·본문·별점이 채워진 수정 폼. */
    @GetMapping("/reviews/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ReviewDetailView review = reviewService.getDetail(id);
        if (!model.containsAttribute("form")) {
            ReviewEditForm form = new ReviewEditForm();
            form.setTitle(review.getTitle());
            form.setContent(review.getContent());
            form.setRating(review.getRating());
            model.addAttribute("form", form);
        }
        model.addAttribute("review", review);
        model.addAttribute("pageTitle", common.message("review.edit.title"));
        model.addAttribute("pageDescription", common.getSiteDescription());
        return "reviews/edit";
    }

    /** REQ-IF-016, REQ-FUNC-007: 성공 → 302 상세. 불일치·검증 실패 → 200 폼 재표시. */
    @PostMapping("/reviews/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Validated @ModelAttribute("form") ReviewEditForm form,
                       BindingResult bindingResult,
                       Model model) {
        ReviewDetailView review = reviewService.getDetail(id);
        model.addAttribute("review", review);
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", common.message("review.edit.title"));
            model.addAttribute("pageDescription", common.getSiteDescription());
            return "reviews/edit";
        }
        ReviewService.UpdateResult result = reviewService.update(id, form.getPassword(),
                form.getTitle(), form.getContent(), form.getRating());
        if (result == ReviewService.UpdateResult.WRONG_PASSWORD) {
            bindingResult.rejectValue("password", "error",
                    common.message("password.mismatch"));
            model.addAttribute("pageTitle", common.message("review.edit.title"));
            model.addAttribute("pageDescription", common.getSiteDescription());
            return "reviews/edit";
        }
        return "redirect:/reviews/" + id;
    }

    /** REQ-IF-017, REQ-FUNC-008: 성공 → 302 목록+flash. 불일치 → 302 상세+1회 메시지. */
    @PostMapping("/reviews/{id}/delete")
    public String delete(@PathVariable Long id,
                         @ModelAttribute("reviewDeleteForm") ReviewDeleteForm form,
                         RedirectAttributes redirectAttributes) {
        ReviewService.DeleteResult result = reviewService.delete(id, form.getPassword());
        if (result == ReviewService.DeleteResult.WRONG_PASSWORD) {
            redirectAttributes.addFlashAttribute("flashError",
                    common.message("password.mismatch"));
            return "redirect:/reviews/" + id;
        }
        redirectAttributes.addFlashAttribute("flashMessage",
                common.message("review.deleted"));
        return "redirect:/reviews";
    }
}
