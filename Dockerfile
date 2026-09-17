# taste-review 실행 이미지 (Role C).
# 빌드 단계: Gradle wrapper로 실행 jar 생성(테스트 제외, 의존성 캐시 우선).
# 실행 단계: JRE 17 + jar + HEALTHCHECK용 curl만 담고 root가 아닌 사용자로 실행한다.
# 포트는 EXPOSE로 고정하지 않고 환경 변수 PORT를 따른다(없으면 8080, 바인딩은 application.yaml).

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon \
  && cp "$(ls build/libs/*.jar | grep -v -- '-plain\.jar' | head -n 1)" /tmp/app.jar

FROM eclipse-temurin:17-jre-jammy
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75
WORKDIR /app
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/* \
  && groupadd -r app \
  && useradd -r -g app -d /app -s /usr/sbin/nologin app
COPY --from=build --chown=app:app /tmp/app.jar /app/app.jar
USER app
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -fsS "http://127.0.0.1:${PORT:-8080}/actuator/health" | grep -q '"status":"UP"'
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
