package org.example.tastereview.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Must 기능 요구사항(MockMvc, Given-When-Then) 추적 테스트. REQ-NF-031.
 * 정상 1 + 예외 1을 FUNC마다 둔다. 보안·SEO·헬스는 SecuritySeoActuatorContractTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewEndpointContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String PASSWORD = "pass1234";

    /** 유효한 리뷰를 등록하고 새 리뷰 id(문자열)를 돌려준다. */
    private String createReviewId(String storeName, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/reviews").with(csrf())
                        .param("storeName", storeName)
                        .param("storeAddress", "서울 종로구 율곡로 10")
                        .param("storeRegion", "서울 종로구")
                        .param("storeCategory", "한식")
                        .param("nickname", "테이스터")
                        .param("password", PASSWORD)
                        .param("rating", "5")
                        .param("title", title)
                        .param("content", "맛있어요 정말 맛있어요 또 가고 싶어요"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String location = result.getResponse().getRedirectedUrl();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private Long storeIdOf(String storeName) {
        return jdbcTemplate.queryForObject(
                "select id from store where name = ?", Long.class, storeName);
    }

    @Test
    @DisplayName("REQ-FUNC-001 정상: 작성 폼 표시")
    void newForm() throws Exception {
        // Given: 빈 상태
        // When: GET /reviews/new
        // Then: 200 + 작성 폼 문구
        mockMvc.perform(get("/reviews/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("가게 찾기")))
                .andExpect(content().string(containsString("새 가게 입력")));
    }

    @Test
    @DisplayName("REQ-FUNC-003 예외: 없는 가게 선택 시 안내")
    void newFormWithMissingStore() throws Exception {
        // Given: 존재하지 않는 가게 id
        // When: GET /reviews/new?storeId=999999
        // Then: 200 + 선택 실패 안내, 새 가게 폼 유지
        mockMvc.perform(get("/reviews/new").param("storeId", "999999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("선택한 가게를 찾을 수 없습니다")))
                .andExpect(content().string(containsString("새 가게 입력")));
    }

    @Test
    @DisplayName("REQ-FUNC-002 정상: 기존 가게 찾기")
    void findStoreCandidates() throws Exception {
        // Given: 등록된 가게
        createReviewId("종로김밥원조", "원조집 후기");
        // When: 이름 일부로 찾기
        // Then: 200 + 후보와 선택 링크
        mockMvc.perform(get("/reviews/new").param("storeQuery", "종로김밥"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("종로김밥원조")))
                .andExpect(content().string(containsString("이 가게 선택")));
    }

    @Test
    @DisplayName("REQ-FUNC-002 예외: 빈 검색어는 후보 없이 폼만")
    void findStoreWithBlankQuery() throws Exception {
        // Given: 공백 검색어
        // When: GET /reviews/new?storeQuery=%20
        // Then: 200 + 결과 없음 안내 없음
        mockMvc.perform(get("/reviews/new").param("storeQuery", " "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("가게 찾기")))
                .andExpect(content().string(not(containsString("일치하는 가게가 없습니다"))));
    }

    @Test
    @DisplayName("REQ-FUNC-003 정상: 기존 가게 선택 시 읽기 전용 표시")
    void selectStore() throws Exception {
        // Given: 등록된 가게
        createReviewId("선택가게", "선택 가게 후기");
        Long storeId = storeIdOf("선택가게");
        // When: storeId로 작성 폼 요청
        // Then: 200 + 선택한 가게 표시
        mockMvc.perform(get("/reviews/new").param("storeId", String.valueOf(storeId)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("선택한 가게")))
                .andExpect(content().string(containsString("선택가게")));
    }

    @Test
    @DisplayName("REQ-FUNC-004 정상: 리뷰 등록 후 상세로 리다이렉트")
    void createReview() throws Exception {
        // Given: 유효한 입력
        // When: POST /reviews
        // Then: 302 상세, 상세에서 제목 확인
        String id = createReviewId("등록가게", "등록 후기 제목");
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("등록 후기 제목")));
    }

    @Test
    @DisplayName("REQ-FUNC-004 예외: 검증 실패 시 폼 재표시")
    void createReviewValidationFailure() throws Exception {
        // Given: 제목이 빈 입력
        int before = jdbcTemplate.queryForObject("select count(*) from review", Integer.class);
        // When: POST /reviews
        // Then: 200 + 항목 오류, 저장 없음
        mockMvc.perform(post("/reviews").with(csrf())
                        .param("storeName", "검증가게")
                        .param("storeAddress", "서울 종로구 율곡로 11")
                        .param("storeRegion", "서울 종로구")
                        .param("storeCategory", "한식")
                        .param("nickname", "테이스터")
                        .param("password", PASSWORD)
                        .param("rating", "4")
                        .param("title", "")
                        .param("content", "열 자가 넘는 내용입니다 열 자가 넘는 내용입니다"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("제목을 입력하세요")));
        int after = jdbcTemplate.queryForObject("select count(*) from review", Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(before, after);
    }

    @Test
    @DisplayName("REQ-FUNC-005 정상: 목록과 홈이 같은 화면")
    void listAndHome() throws Exception {
        // Given: 등록된 리뷰
        createReviewId("목록가게", "목록 노출 제목");
        // When: GET /, GET /reviews
        // Then: 200 + 제목 노출
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("최신 리뷰")))
                .andExpect(content().string(containsString("목록 노출 제목")));
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("목록 노출 제목")));
    }

    @Test
    @DisplayName("REQ-FUNC-005 예외: 마지막 페이지 초과 시 안내")
    void listPageOverflow() throws Exception {
        // Given: 리뷰 없음
        // When: GET /reviews?page=99999
        // Then: 200 + 비어 있음 안내
        mockMvc.perform(get("/reviews").param("page", "99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("아직 리뷰가 없습니다.")));
    }

    @Test
    @DisplayName("REQ-FUNC-006 정상: 리뷰 상세")
    void detail() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId("상세가게", "상세 제목");
        // When: GET /reviews/{id}
        // Then: 200 + 본문·가게·댓글 영역
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("상세 제목")))
                .andExpect(content().string(containsString("상세가게")))
                .andExpect(content().string(containsString("맛있어요")));
    }

    @Test
    @DisplayName("REQ-FUNC-006 예외: 없는 리뷰는 404")
    void detailMissing() throws Exception {
        // Given: 존재하지 않는 id
        // When: GET /reviews/999999
        // Then: 404 오류 페이지, 스택 없음
        mockMvc.perform(get("/reviews/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("페이지를 찾을 수 없습니다")))
                .andExpect(content().string(containsString("홈으로")))
                .andExpect(content().string(not(containsString("Trace"))));
    }

    @Test
    @DisplayName("REQ-FUNC-006 예외: 숫자가 아닌 id는 404")
    void detailNonNumericId() throws Exception {
        // Given: 숫자가 아닌 id
        // When: GET /reviews/abc
        // Then: 404 오류 페이지
        mockMvc.perform(get("/reviews/abc"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("페이지를 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("REQ-FUNC-007 정상: 리뷰 수정")
    void editReview() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId("수정가게", "수정 전 제목");
        mockMvc.perform(get("/reviews/" + id + "/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("리뷰 수정")));
        // When: 올바른 비밀번호로 수정
        // Then: 302 상세, 본문에 수정됨 표시
        mockMvc.perform(post("/reviews/" + id + "/edit").with(csrf())
                        .param("password", PASSWORD)
                        .param("title", "수정 후 제목")
                        .param("content", "수정한 본문입니다 열 자가 넘어야 합니다")
                        .param("rating", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/" + id));
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("수정 후 제목")))
                .andExpect(content().string(containsString("수정됨")));
    }

    @Test
    @DisplayName("REQ-FUNC-007 예외: 비밀번호 불일치 시 수정 안 함")
    void editReviewWrongPassword() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId("수정실패가게", "원래 제목");
        // When: 틀린 비밀번호로 수정
        // Then: 200 + 불일치 안내, 원본 유지
        mockMvc.perform(post("/reviews/" + id + "/edit").with(csrf())
                        .param("password", "wrong-pass")
                        .param("title", "바뀌면 안 되는 제목")
                        .param("content", "바뀌면 안 되는 본문입니다 열 자가 넘어야 합니다")
                        .param("rating", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("비밀번호가 일치하지 않습니다")));
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(content().string(containsString("원래 제목")));
    }

    @Test
    @DisplayName("REQ-FUNC-008 정상: 리뷰 삭제")
    void deleteReview() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId("삭제가게", "삭제될 제목");
        // When: 올바른 비밀번호로 삭제
        // Then: 302 목록 + 1회 안내, 상세는 404
        MvcResult result = mockMvc.perform(post("/reviews/" + id + "/delete").with(csrf())
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews"))
                .andReturn();
        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/reviews").session(session))
                .andExpect(content().string(containsString("리뷰가 삭제되었습니다")));
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("REQ-FUNC-008 예외: 비밀번호 불일치 시 삭제 안 함")
    void deleteReviewWrongPassword() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId("삭제실패가게", "남아야 할 제목");
        // When: 틀린 비밀번호로 삭제
        // Then: 302 상세 + 1회 안내, 리뷰 유지
        MvcResult result = mockMvc.perform(post("/reviews/" + id + "/delete").with(csrf())
                        .param("password", "wrong-pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/" + id))
                .andReturn();
        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/reviews/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("비밀번호가 일치하지 않습니다")))
                .andExpect(content().string(containsString("남아야 할 제목")));
    }

    @Test
    @DisplayName("REQ-FUNC-010 정상: 키워드 검색")
    void searchByKeyword() throws Exception {
        // Given: 서로 다른 리뷰 2개
        createReviewId("검색가게하나", "종로김밥 원조맛집");
        createReviewId("검색가게둘", "강남파스타 전문점");
        // When: 키워드로 검색
        // Then: 일치하는 리뷰만 노출
        mockMvc.perform(get("/reviews").param("keyword", "원조맛집"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("종로김밥 원조맛집")))
                .andExpect(content().string(not(containsString("강남파스타 전문점"))));
    }

    @Test
    @DisplayName("REQ-FUNC-010 예외: 결과 없음 안내와 초기화 링크")
    void searchNoResult() throws Exception {
        // Given: 검색어와 무관한 리뷰
        createReviewId("무관가게", "무관한 제목");
        // When: 일치하지 않는 검색어
        // Then: 안내 + 초기화 링크
        mockMvc.perform(get("/reviews").param("keyword", "없는검색어ZZZ"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("검색 결과가 없습니다.")))
                .andExpect(content().string(containsString("href=\"/reviews\"")));
    }

    @Test
    @DisplayName("REQ-FUNC-011 정상: 댓글 작성")
    void addComment() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId("댓글가게", "댓글 대상 후기");
        // When: 유효한 댓글 등록
        // Then: 302 상세#comments, 상세에서 댓글 확인
        mockMvc.perform(post("/reviews/" + id + "/comments").with(csrf())
                        .param("nickname", "댓글러")
                        .param("password", "cmt-pass1")
                        .param("content", "공감합니다 좋은 후기예요"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/" + id + "#comments"));
        mockMvc.perform(get("/reviews/" + id))
                .andExpect(content().string(containsString("공감합니다 좋은 후기예요")));
    }

    @Test
    @DisplayName("REQ-FUNC-011 예외: 댓글 검증 실패 시 상세 재표시")
    void addCommentValidationFailure() throws Exception {
        // Given: 등록된 리뷰
        String id = createReviewId("댓글실패가게", "댓글 실패 대상");
        // When: 빈 내용으로 댓글 등록
        // Then: 200 + 항목 오류
        mockMvc.perform(post("/reviews/" + id + "/comments").with(csrf())
                        .param("nickname", "댓글러")
                        .param("password", "cmt-pass1")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("내용을 입력하세요")));
    }

    @Test
    @DisplayName("REQ-FUNC-012 정상: 댓글 삭제")
    void deleteComment() throws Exception {
        // Given: 댓글 있는 리뷰
        String reviewId = createReviewId("댓글삭제가게", "댓글 삭제 대상");
        mockMvc.perform(post("/reviews/" + reviewId + "/comments").with(csrf())
                        .param("nickname", "댓글러")
                        .param("password", "cmt-pass1")
                        .param("content", "지워질 댓글입니다"))
                .andExpect(status().is3xxRedirection());
        Long commentId = jdbcTemplate.queryForObject(
                "select id from comment where review_id = ? order by id desc limit 1",
                Long.class, Long.valueOf(reviewId));
        // When: 올바른 비밀번호로 삭제
        // Then: 302 상세#comments, 댓글 사라짐
        mockMvc.perform(post("/comments/" + commentId + "/delete").with(csrf())
                        .param("password", "cmt-pass1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/" + reviewId + "#comments"));
        mockMvc.perform(get("/reviews/" + reviewId))
                .andExpect(content().string(not(containsString("지워질 댓글입니다"))));
    }

    @Test
    @DisplayName("REQ-FUNC-012 예외: 비밀번호 불일치 시 댓글 유지")
    void deleteCommentWrongPassword() throws Exception {
        // Given: 댓글 있는 리뷰
        String reviewId = createReviewId("댓글삭제실패가게", "댓글 유지 대상");
        mockMvc.perform(post("/reviews/" + reviewId + "/comments").with(csrf())
                        .param("nickname", "댓글러")
                        .param("password", "cmt-pass1")
                        .param("content", "남아야 할 댓글입니다"))
                .andExpect(status().is3xxRedirection());
        Long commentId = jdbcTemplate.queryForObject(
                "select id from comment where review_id = ? order by id desc limit 1",
                Long.class, Long.valueOf(reviewId));
        // When: 틀린 비밀번호로 삭제
        // Then: 302 상세 + 1회 안내, 댓글 유지
        MvcResult result = mockMvc.perform(post("/comments/" + commentId + "/delete").with(csrf())
                        .param("password", "wrong-pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/" + reviewId + "#comments"))
                .andReturn();
        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/reviews/" + reviewId).session(session))
                .andExpect(content().string(containsString("비밀번호가 일치하지 않습니다")))
                .andExpect(content().string(containsString("남아야 할 댓글입니다")));
    }

    @Test
    @DisplayName("REQ-FUNC-013 정상: 없는 경로는 404 페이지")
    void unknownPath() throws Exception {
        // Given: 없는 경로
        // When: GET /no-such-page
        // Then: 404 오류 페이지
        mockMvc.perform(get("/no-such-page"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("페이지를 찾을 수 없습니다")))
                .andExpect(content().string(containsString("홈으로")));
    }

    @Test
    @DisplayName("REQ-FUNC-009 정상: 가게 상세")
    void storeDetail() throws Exception {
        // Given: 리뷰 있는 가게
        createReviewId("상세가게둘", "가게 상세용 후기");
        Long storeId = storeIdOf("상세가게둘");
        // When: GET /stores/{id}
        // Then: 200 + 가게 정보와 리뷰
        mockMvc.perform(get("/stores/" + storeId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("상세가게둘")))
                .andExpect(content().string(containsString("가게 상세용 후기")));
    }

    @Test
    @DisplayName("REQ-FUNC-009 예외: 없는 가게는 404")
    void storeDetailMissing() throws Exception {
        // Given: 존재하지 않는 가게 id
        // When: GET /stores/999999
        // Then: 404 오류 페이지
        mockMvc.perform(get("/stores/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("페이지를 찾을 수 없습니다")));
    }
}
