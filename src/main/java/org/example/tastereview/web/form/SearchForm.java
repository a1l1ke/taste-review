package org.example.tastereview.web.form;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.example.tastereview.web.dto.PageWindow;
import org.example.tastereview.web.support.Normalize;

/**
 * REQ-FUNC-010 검색 파라미터. 초과 길이는 자르고 잘못된 값은 무시한다.
 * 숫자 바인딩 실패 시 400을 피하기 위해 원시 문자열로 받아 정제한다.
 */
public class SearchForm {

    public static final String SORT_LATEST = "latest";
    public static final String SORT_RATING = "rating";

    private final String keyword;
    private final String region;
    private final String category;
    private final Integer minRating;
    private final String sort;
    private final int page;

    private SearchForm(String keyword, String region, String category,
                       Integer minRating, String sort, int page) {
        this.keyword = keyword;
        this.region = region;
        this.category = category;
        this.minRating = minRating;
        this.sort = sort;
        this.page = page;
    }

    public static SearchForm sanitize(String keyword, String region, String category,
                                      String minRating, String sort, String page,
                                      List<String> allowedCategories) {
        String cleanKeyword = truncate(Normalize.plain(keyword), 100);
        String cleanRegion = truncate(Normalize.plain(region), 30);
        String cleanCategory = allowedCategories != null && allowedCategories.contains(category)
                ? category : null;
        Integer cleanMinRating = parseMinRating(minRating);
        String cleanSort = SORT_RATING.equals(sort) ? SORT_RATING : SORT_LATEST;
        return new SearchForm(cleanKeyword, cleanRegion, cleanCategory,
                cleanMinRating, cleanSort, parsePage(page));
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    private static Integer parseMinRating(String value) {
        if (Normalize.isBlank(value)) {
            return null;
        }
        try {
            int rating = Integer.parseInt(value.trim());
            return rating >= 1 && rating <= 5 ? rating : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parsePage(String value) {
        return PageWindow.parsePage(value);
    }

    public boolean hasKeyword() {
        return keyword != null;
    }

    public boolean hasRegion() {
        return region != null;
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasMinRating() {
        return minRating != null;
    }

    /** 검색 조건이 하나라도 있으면 true. 빈 상태 문구 선택에 쓴다. */
    public boolean isFiltered() {
        return hasKeyword() || hasRegion() || hasCategory() || hasMinRating();
    }

    public String getKeyword() {
        return keyword;
    }

    public String getRegion() {
        return region;
    }

    public String getCategory() {
        return category;
    }

    public Integer getMinRating() {
        return minRating;
    }

    public String getSort() {
        return sort;
    }

    public int getPage() {
        return page;
    }

    /** 페이지 번호를 제외한 현재 조건 쿼리스트링. 페이지 링크가 조건을 유지한다(REQ-FUNC-005). */
    public String toQueryString() {
        StringBuilder sb = new StringBuilder();
        append(sb, "keyword", keyword);
        append(sb, "region", region);
        append(sb, "category", category);
        append(sb, "minRating", minRating == null ? null : minRating.toString());
        append(sb, "sort", sort);
        return sb.toString();
    }

    private void append(StringBuilder sb, String name, String value) {
        if (value == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append('&');
        }
        try {
            sb.append(name).append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            sb.append(name).append('=').append(value);
        }
    }
}
