package org.example.tastereview.deploy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 배포 산출물 정적 계약. REQ-NF-020~024, REQ-NF-026, REQ-IF-030/031 지원.
 */
class DeployFilesContractTest {

    private String read(String relative) throws IOException {
        Path direct = Path.of(relative);
        Path file = Files.exists(direct) ? direct : Path.of(System.getProperty("user.dir"), relative);
        assertThat(file).exists();
        return Files.readString(file);
    }

    @Test
    @DisplayName("REQ-NF-020/021/022: 멀티스테이지·비root·ENV·캐시 순서·테스트 스킵")
    void dockerfile() throws IOException {
        String dockerfile = read("Dockerfile");
        // 멀티스테이지(빌드 JDK → 실행 JRE)
        assertThat(dockerfile).contains("AS build");
        assertThat(dockerfile).contains("eclipse-temurin:17-jdk");
        assertThat(dockerfile).contains("eclipse-temurin:17-jre");
        // 의존성 먼저, 소스 나중
        assertThat(dockerfile.indexOf("COPY gradle")).isLessThan(dockerfile.indexOf("COPY src"));
        // 테스트 스킵
        assertThat(dockerfile).contains("bootJar -x test");
        // 비root
        assertThat(dockerfile).contains("useradd");
        assertThat(dockerfile).containsPattern("(?m)^USER (?!root).+");
        // 덮어 가능한 기본값
        assertThat(dockerfile).contains("ENV SPRING_PROFILES_ACTIVE=prod");
        assertThat(dockerfile).contains("JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75");
        // PORT 사용, EXPOSE 지시어 없음(주석 언급 제외)
        assertThat(dockerfile).contains("${PORT:-8080}");
        assertThat(dockerfile).doesNotMatch("(?m)^EXPOSE\\b.*");
        // 헬스 체크
        assertThat(dockerfile).contains("HEALTHCHECK");
        assertThat(dockerfile).contains("/actuator/health");
    }

    @Test
    @DisplayName("REQ-NF-023: dockerignore·gitignore 제외 목록")
    void ignoreFiles() throws IOException {
        String dockerignore = read(".dockerignore");
        assertThat(dockerignore).contains("build/", ".gradle/", ".idea/", ".git/", "docs/", ".env*");
        String gitignore = read(".gitignore");
        assertThat(gitignore).contains(".env*");
    }

    @Test
    @DisplayName("REQ-NF-026: render.yaml Blueprint(비밀 sync:false, 헬스 경로)")
    void renderBlueprint() throws IOException {
        String blueprint = read("render.yaml");
        assertThat(blueprint).contains("runtime: docker");
        assertThat(blueprint).contains("healthCheckPath: /actuator/health");
        assertThat(blueprint).contains("DB_URL");
        assertThat(blueprint).contains("DB_USERNAME");
        assertThat(blueprint).contains("DB_PASSWORD");
        assertThat(blueprint).contains("sync: false");
        assertThat(blueprint).doesNotContain("password: ");
    }

    @Test
    @DisplayName("REQ-NF-024/IF-030/031: 배포 문서(JDBC 변환·pooled·환경변수)")
    void deployDoc() throws IOException {
        String doc = read("docs/deploy.md");
        assertThat(doc).contains("jdbc:postgresql://");
        assertThat(doc).contains("sslmode=require");
        assertThat(doc).contains("-pooler");
        assertThat(doc).contains("SPRING_PROFILES_ACTIVE");
        assertThat(doc).contains("PORT");
        assertThat(doc).contains("/actuator/health");
    }

    @Test
    @DisplayName("결정 기록 존재")
    void decisionRecord() throws IOException {
        read("docs/decisions/DEC-003-infra.md");
    }
}
