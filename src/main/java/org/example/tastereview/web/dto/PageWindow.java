package org.example.tastereview.web.dto;

import java.util.ArrayList;
import java.util.List;

/** DESIGN 7.9 페이지 이동: 이전·다음과 현재 페이지 앞뒤 2개 번호만 노출한다. */
public class PageWindow {

    private final int currentPage;
    private final int totalPages;
    private final List<Integer> pageNumbers;

    private PageWindow(int currentPage, int totalPages, List<Integer> pageNumbers) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageNumbers = pageNumbers;
    }

    public static PageWindow of(int currentPage, long totalPages) {
        return of(currentPage, (int) Math.max(1, Math.min(totalPages, Integer.MAX_VALUE)));
    }

    public static PageWindow of(int currentPage, int totalPages) {
        int total = Math.max(totalPages, 1);
        int current = Math.min(Math.max(currentPage, 1), total);
        List<Integer> numbers = new ArrayList<>();
        for (int page = Math.max(1, current - 2); page <= Math.min(total, current + 2); page++) {
            numbers.add(page);
        }
        return new PageWindow(current, total, numbers);
    }

    public boolean hasPrevious() {        return currentPage > 1;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    public int getPreviousPage() {
        return Math.max(1, currentPage - 1);
    }

    public int getNextPage() {
        return Math.min(totalPages, currentPage + 1);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<Integer> getPageNumbers() {
        return pageNumbers;
    }

    /** page 파라미터 정제: 1 미만·숫자 아님 → 1 (REQ-FUNC-010). */
    public static int parsePage(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(value.trim());
            return page >= 1 ? page : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
