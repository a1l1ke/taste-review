package org.example.tastereview.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 보안·SEO·헬스 추적 테스트. REQ-NF-005~008, REQ-IF-001(SEO), REQ-IF-021, REQ-FUNC-013(403).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecuritySeoActuatorContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "s3cret-PW-9876";

    private String createReviewId() throws Exception {
        MvcResult result = mockMvc.perform(post("/reviews").with(csrf())
                        .param("storeName", "보안가게")
                        .param("storeAddress", "서울 종로구 율곡로 12")
                        .param("storeRegion", "서울 종로구")
                        .param("storeCategory", "한식")
                        .param("nickname", "보안테스터")
                        .param("password", PASSWORD)
                        .param("rating", "5")
                        .param("title", "보안 확인용 후기")
                        .param("content", "보안 확인용 본문입니다 열 자가 넘어야 합니다"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String location = result.getResponse().getRedirectedUrl();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    @Test
    @DisplayName("REQ-NF-005: 로그인 없이 허용, 로그인 리다이렉트 없음")
    void permitAllWithoutRedirect() throws Exception {
        // Given: Security permitAll 구성
        // When: 공개 페이지 요청
        // Then: 200, 로그인 경로로 보내지 않음
        mockMvc.perform(get("/reviews/new"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(not(containsString("/login"))));
    }

    @Test
    @DisplayName("REQ-NF-006: CSRF 토큰 없는 POST는 403")
    void csrfMissingForbidden() throws Exception {
        // Given: CSRF 토큰 없음
        // When: POST /reviews
        // Then: 403 (필터 단계 거부라 MockMvc 본문은 비어 있음.
        //   실컨테이너는 JSON 403을 돌려주므로 레이아웃 오류 페이지 연계는 A/B 후속 작업으로 보고)
        mockMvc.perform(post("/reviews")
                        .param("nickname", "x")
                        .param("password", "xxxx")
                        .param("title", "x"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("REQ-NF-007: 작성 비밀번호는 BCrypt 해시로만 저장")
    void passwordStoredAsBcryptHash() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId();
        // When: DB 해시 조회
        String hash = jdbcTemplate.queryForObject(
                "select password_hash from review where id = ?", String.class, Long.valueOf(id));
        // Then: 평문 없음, BCrypt 형식, 대조 성공
        assertTrue(hash.startsWith("$2"), "BCrypt 해시 형식");
        assertFalse(hash.contains(PASSWORD), "평문 미포함");
        assertTrue(passwordEncoder.matches(PASSWORD, hash), "대조 성공");
    }

    @Test
    @DisplayName("REQ-NF-007: 비밀번호 평문을 화면에 노출하지 않음")
    void passwordNotExposedInPages() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId();
        // When: 상세·목록 조회
        // Then: 평문 없음
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(content().string(not(containsString(PASSWORD))));
        mockMvc.perform(get("/reviews"))
                .andExpect(content().string(not(containsString(PASSWORD))));
    }

    @Test
    @DisplayName("REQ-NF-008: 사용자 입력은 HTML 이스케이프")
    void userInputEscaped() throws Exception {
        // Given: script를 담은 댓글
        String id = createReviewId();
        mockMvc.perform(post("/reviews/" + id + "/comments").with(csrf())
                        .param("nickname", "댓글러")
                        .param("password", "cmt-pass1")
                        .param("content", "<script>alert(1)</script>"))
                .andExpect(status().is3xxRedirection());
        // When: 상세 조회
        // Then: 이스케이프되어 글자로 표시
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    @DisplayName("REQ-IF-001 SEO: 목록 title·meta description")
    void listSeo() throws Exception {
        // Given: 목록 화면
        // When: GET /reviews
        // Then: 페이지별 title과 description
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>최신 리뷰</title>")))
                .andExpect(content().string(containsString("name=\"description\"")))
                .andExpect(content().string(containsString("솔직한 맛집 후기를 나누는 곳")));
    }

    @Test
    @DisplayName("REQ-IF-001 SEO: 상세 title 형식")
    void detailSeo() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId();
        // When: GET /reviews/{id}
        // Then: "제목 - 가게 | 사이트" 형식
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<title>보안 확인용 후기 - 보안가게 | 맛집 리뷰</title>")));
    }

    @Test
    @DisplayName("REQ-IF-021: actuator health는 UP")
    void actuatorHealth() throws Exception {
        // Given: 실행 중인 앱
        // When: GET /actuator/health
        // Then: 200 {"status":"UP"}, 세부 정보 없음
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(content().string(not(containsString("db"))));
    }
}
