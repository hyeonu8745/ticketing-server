# DEAR TICKET — Backend

![CI](https://github.com/hyeonu8745/ticketing-server/actions/workflows/ci.yml/badge.svg)

> 고가용성 아키텍처 기반의 실시간 티켓팅 시스템
> Spring Boot + Redis 분산 락

---

## Tech Stack

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security + JWT |
| Database | MySQL 8.0 |
| Cache / 분산 락 | Redis 7 + Redisson |
| 모니터링 | Prometheus + Grafana |
| 인프라 | Docker, Docker Compose |
| API 문서 | SpringDoc OpenAPI (Swagger) |
| CI/CD | GitHub Actions |

---

## 프로젝트 구조

```
src/main/java/com/ticketing/server/
├── config/          # Security, Redis, Web, 관리자 계정 초기화
├── controller/      # REST API 컨트롤러
├── domain/          # JPA 엔티티
├── dto/             # 요청/응답 DTO
├── repository/      # Spring Data JPA 레포지토리
├── service/         # 비즈니스 로직
└── TicketingServerApplication.java
```

---

## 주요 기능

### 인증 / 인가
- JWT 기반 Stateless 인증
- ROLE_USER / ROLE_ADMIN 권한 분리
- 서버 기동 시 관리자 계정 자동 생성

### 예매 & 동시성 제어
- Redisson 분산 락(Distributed Lock)으로 중복 예매 완전 방지
- ReservationFacade 패턴으로 락 로직 단일화
- Redis 기반 실시간 대기열(Queue) 시스템
- 포인트 결제 및 환불 처리
- 예매 취소 시 소프트 딜리트(soft delete)

### AI 마이크로서비스 연동
- 봇 탐지 / 개인화 추천 / 수요 예측 / 챗봇 서버와 RestTemplate으로 통신
- 모든 AI 서버는 Fail-Open 전략 적용 (AI 서버 장애 시 메인 서비스 중단 없음)
- AI 서버 레포지토리 → [DEAR TICKET AI](https://github.com/hyeonu8745/ticketing-ai)

### 모니터링
- Spring Boot Actuator → `/actuator/prometheus` 메트릭 노출
- Prometheus 15초 주기 수집
- Grafana 대시보드: JVM 메모리, HTTP 요청율, 응답 지연 p95/p99, Tomcat 스레드

### 공연 데이터
- KOPIS 공연예술통합전산망 API 연동
- 관리자 콘솔에서 원하는 건수만큼 동기화 가능

---

## 실행 방법

### 1. 환경변수 설정

`.env` 파일을 프로젝트 루트에 생성:

```env
KOPIS_API_KEY=your_kopis_api_key_here
```

### 2. 인프라 실행 (Docker)

```bash
docker-compose up -d
```

MySQL(3306), Redis(6379), Prometheus(9090), Grafana(3000) 컨테이너가 실행됩니다.

### 3. 개발 환경 실행

IntelliJ에서 `TicketingServerApplication` 실행
(Run Configuration → Environment Variables에 `.env` 파일 연결)

### 4. 프로덕션 빌드 및 실행 (배포)

```bash
# 빌드
gradlew.bat build -x test

# 8080 포트 실행
java -jar build\libs\ticketing-server-0.0.1-SNAPSHOT.jar --server.port=8080 --kopis.api-key=키값

# 8081 포트 실행 (로드밸런싱용, 새 터미널에서)
java -jar build\libs\ticketing-server-0.0.1-SNAPSHOT.jar --server.port=8081 --kopis.api-key=키값
```

> AI 서버(uvicorn 4개)를 먼저 실행한 후 백엔드를 기동하는 것을 권장합니다.

---

## 주요 설정 (application.yml)

| 항목 | 값 |
|------|-----|
| Tomcat 최대 스레드 | 400 |
| DB 커넥션 풀 | 50 |
| Redis 커넥션 풀 | 100 |
| JWT 만료 시간 | 1시간 |

---

## API 문서

서버 실행 후 아래 주소에서 Swagger UI 확인:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 인프라 구성

```
[Client]
    │
    ▼
[Cloudflare Tunnel]
    │
    ▼
[Nginx :80] ── 로드밸런싱 ──→ [Spring Boot :8080]
                           └─→ [Spring Boot :8081]
                                    │
                         ┌──────────┴──────────┐
                         ▼                     ▼
                   [MySQL :3306]         [Redis :6379]

[Prometheus :9090] ←── scrape ── [Spring Boot Actuator]
[Grafana :3000]    ←── query  ── [Prometheus]
```

---

## 관련 레포지토리

- 프론트엔드: [DEAR TICKET Frontend](https://github.com/hyeonu8745/ticketing-frontend)
- AI 서버: [DEAR TICKET AI](https://github.com/hyeonu8745/ticketing-ai)

---

## References

- [KOPIS 공연예술통합전산망](https://www.kopis.or.kr) — 공연 데이터 제공