package org.example.tastereview.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code app.*} 업무 설정값의 타입안전 바인딩 (REQ-NF-011, ADR-007).
 * 환경 변수(APP_PAGINATION_PAGE_SIZE 등)로 덮어쓸 수 있다.
 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @Valid
    private Site site = new Site();

    @Valid
    private Pagination pagination = new Pagination();

    @Valid
    private Store store = new Store();

    @Valid
    private Display display = new Display();

    @Valid
    private Security security = new Security();

    @Getter
    @Setter
    public static class Site {
        @NotBlank
        private String name = "맛집 리뷰";
        @NotBlank
        private String description = "솔직한 맛집 후기를 나누는 곳";
    }

    @Getter
    @Setter
    public static class Pagination {
        @Min(1)
        private int pageSize = 20;
    }

    @Getter
    @Setter
    public static class Store {
        @Min(1)
        private int searchLimit = 10;
        @NotEmpty
        private List<String> categories;
    }

    @Getter
    @Setter
    public static class Display {
        @NotBlank
        private String timeZone = "Asia/Seoul";
    }

    @Getter
    @Setter
    public static class Security {
        @Min(4)
        private int bcryptStrength = 10;
    }
}
