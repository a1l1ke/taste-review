package org.example.tastereview.style;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DESIGN.md 2~9절 CSS 정적 계약. REQ-IF-001(스타일·레이아웃) 지원.
 * 토큰 정의는 :root에서만, 사용처는 var()·단위 없는 수·백분율·키워드·calc만 둔다.
 * 미디어 조건 값(min-width 600/960)은 CSS 변수로 둘 수 없어 예외로 둔다.
 */
class AppCssContractTest {

    private static final Path CSS = resolve("src/main/resources/static/css/app.css");

    private static Path resolve(String relative) {
        Path direct = Path.of(relative);
        if (Files.exists(direct)) {
            return direct;
        }
        return Path.of(System.getProperty("user.dir"), relative);
    }

    private String css() throws IOException {
        return Files.readString(CSS);
    }

    private String rootBlock(String css) {
        int start = css.indexOf(":root");
        int end = css.indexOf('}', start);
        return css.substring(start, end);
    }

    /** :root와 미디어 조건을 제외한 사용처 코드. */
    private String usage(String css) {
        String noRoot = css.substring(css.indexOf('}', css.indexOf(":root")) + 1);
        return noRoot.replaceAll("@media[^{]*", "");
    }

    @Test
    @DisplayName("DESIGN 2절: 색 토큰 16종 정의")
    void colorTokens() throws IOException {
        String root = rootBlock(css());
        assertThat(root).contains(
                "--color-bg: #FFF8F1",
                "--color-surface: #FFFFFF",
                "--color-surface-muted: #FBEEE2",
                "--color-border: #EBD9C8",
                "--color-border-strong: #A8836A",
                "--color-text: #3B2A20",
                "--color-text-muted: #7A5F4E",
                "--color-primary: #C2410C",
                "--color-primary-hover: #9A3412",
                "--color-on-primary: #FFFFFF",
                "--color-star: #B45309",
                "--color-focus: #EA580C",
                "--color-danger: #B42318",
                "--color-danger-bg: #FDECEA",
                "--color-success: #35702F",
                "--color-success-bg: #EAF4E8");
    }

    @Test
    @DisplayName("DESIGN 3~5절: 글자·간격·모서리·테두리·포커스 토큰 정의")
    void typeSpaceShapeTokens() throws IOException {
        String root = rootBlock(css());
        assertThat(root).contains(
                "--font-family:",
                "--text-xs: 12px", "--text-sm: 14px", "--text-md: 16px",
                "--text-lg: 20px", "--text-xl: 24px", "--text-2xl: 32px",
                "--space-1: 8px", "--space-2: 16px", "--space-3: 24px",
                "--space-4: 32px", "--space-5: 40px", "--space-6: 48px",
                "--radius-sm: 8px", "--radius-md: 16px", "--radius-full: 9999px",
                "--shadow-card:", "--border-width: 1px",
                "--focus-ring:", "--focus-ring-offset:");
        assertThat(root).contains("Pretendard");
    }

    @Test
    @DisplayName("DESIGN 5절·9절: 사용처에 색·수치 직접값 금지")
    void noLiteralValuesInUsage() throws IOException {
        String usage = usage(css());
        assertThat(usage).doesNotContain("#");
        assertThat(usage).doesNotContain("px");
    }

    @Test
    @DisplayName("DESIGN 6절: min-width 600·960 브레이크포인트, max-width 조건 없음")
    void breakpoints() throws IOException {
        String css = css();
        assertThat(css).contains("(min-width: 600px)");
        assertThat(css).contains("(min-width: 960px)");
        assertThat(css).doesNotContain("(max-width:");
    }

    @Test
    @DisplayName("DESIGN 5절: focus-visible 링, outline 제거 없음")
    void focusVisible() throws IOException {
        String css = css();
        assertThat(css).contains(":focus-visible");
        assertThat(css).contains("outline: var(--focus-ring)");
        assertThat(usage(css)).doesNotContain("outline: none");
    }

    @Test
    @DisplayName("DESIGN 6~7절: 터치 48·입력 16·본문 폭·버튼 3종·컴포넌트 선택자")
    void components() throws IOException {
        String css = css();
        // 터치 영역과 입력 글자
        assertThat(css).contains("min-height: var(--space-6)");
        assertThat(css).contains("font-size: var(--text-md)");
        // 본문 폭 960·읽기 폭 720을 간격 변수 calc로 표현
        assertThat(css).contains("calc(var(--space-1) * 120)");
        assertThat(css).contains("calc(var(--space-1) * 90)");
        // 버튼 3종(양쪽 명명 모두 지원)
        assertThat(css).contains(".button-primary");
        assertThat(css).contains(".button-secondary");
        assertThat(css).contains(".button-danger");
        // 템플릿 클래스 커버리지
        List<String> templateClasses = List.of(
                ".page", ".site-header", ".site-header-home", ".site-header-search",
                ".site-footer", ".review-card", ".review-card-title", ".review-meta",
                ".badge", ".rating", ".review-list", ".pagination-current",
                ".search-form", ".field", ".required-mark", ".field-error",
                ".form-help", ".form-error-summary", ".review-form",
                ".store-box", ".review-content", ".review-delete-form",
                ".comment-delete-form", ".comments", ".comment",
                ".empty-state", ".flash-success", ".flash-error", ".error-page");
        assertThat(css).contains(templateClasses);
    }

    @Test
    @DisplayName("DESIGN 1·6절: 단일 파일, 외부 임포트 없음, 가로 스크롤 방지")
    void singleFileNoOverflow() throws IOException {
        String css = css();
        assertThat(css).doesNotContain("@import");
        assertThat(css).contains("box-sizing: border-box");
        assertThat(css).contains("max-width: 100%");
    }
}
