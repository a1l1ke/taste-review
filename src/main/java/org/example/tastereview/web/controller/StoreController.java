package org.example.tastereview.web.controller;

import org.example.tastereview.web.dto.PageWindow;
import org.example.tastereview.web.dto.StoreDetailView;
import org.example.tastereview.web.service.StoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** 가게 상세 (REQ-IF-020, REQ-FUNC-009). */
@Controller
public class StoreController {

    private final StoreService storeService;
    private final WebCommonAdvice common;

    public StoreController(StoreService storeService, WebCommonAdvice common) {
        this.storeService = storeService;
        this.common = common;
    }

    @GetMapping("/stores/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String page,
                         Model model) {
        int currentPage = PageWindow.parsePage(page);
        StoreDetailView store = storeService.getDetail(id, currentPage);
        model.addAttribute("store", store);
        model.addAttribute("pageWindow",
                PageWindow.of(currentPage, store.getTotalPages()));
        model.addAttribute("pageTitle",
                store.getName() + " | " + common.getSiteName());
        model.addAttribute("pageDescription", store.getName() + " - " + store.getAddress());
        return "stores/detail";
    }
}
