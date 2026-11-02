# 세션 이어가기 메모 — 최신 시작점 (11/02 로그인 구현 3번까지)

> 새 세션의 AI에게: 아래 11/02 CSRF 이하 내용은 **지난 기록**이다. 다시 묻지 말 것. 기준은 노션.
> plan.md는 PC 초기화 전에 보존하려고 사용자 요청으로 `feat/user-login`에 커밋함. **PR 전에 지울지 사용자에게 물을 것.** 이어받은 PC에서 `git switch feat/user-login` 후 읽을 것.

- 레포: `feat/user-login` (origin에 push함). 구현 3번까지 커밋 `b746d12` · `8f3bb5b` · `1ee48b7`. PR 없음. `compileJava`만 통과, 테스트 실행 안 함
- 로드맵: F-12 "진행 중" (https://app.notion.com/3fe3fff1d30881a1b886c3f6e3437228)
- 복습 질문: 11/01 사용자 결정대로 먼저 내지 말 것
- 새 PC 준비: `git config user.name "xodbs126"` · `git config user.email "wlsghks@jnu.ac.kr"` (11/02 이 PC에 작성자 설정이 없어 세 커밋에만 일회성으로 적용, 사용자가 고른 값). `.mcp.json`의 IntelliJ 포트는 PC마다 다를 수 있음

## 관리자 구분에서 정한 것 (다시 묻지 말 것)

| 항목 | 결정 | 근거 |
|---|---|---|
| 구분 방법 | A. `app_user`에 칸 추가 | AI 추천을 사용자가 고름. 사용자 근거: 서버 재시작 없이 관리자 변경, 판단 기준이 DB 한 곳. B와 다른 점(로그인 때 `app_user`만 봄)은 AI 보완 |
| 세션에 넣는 값 | 회원 `id`만 (방법 가) | 사용자: 세션에 관리자 여부를 저장하면 DB에서 지워도 세션의 값이 남아 승인이 성공함 |
| 구매자 B가 관리자 API 호출 | 403 | 사용자: 401은 인증, 403은 인가. 다시 로그인해도 권한이 없음 |
| 403 code | `ADMIN_REQUIRED` | AI가 지음. 사용자가 따로 확인하지는 않음 |
| 관리자 계정 생성 | 가입 API로 회원을 만든 뒤 SQL로 표시 | AI 추천을 사용자가 고름. 대가: 환경마다 사람이 SQL 실행 |
| 세션을 다루는 곳 | 컨트롤러. 서비스는 회원 `id`를 돌려줌 | 설명 후 사용자가 정리: 서비스에 두면 호출하는 쪽도 세션을 준비해야 함 |

- 칸 이름 · 타입은 안 정함. Flyway 추가와 ERD 수정은 인가 단계에서
- 방법 가의 손해 문장은 사용자 위임("니 알아서 해라")으로 AI가 작성: 관리자 표시가 지워진 C가 한정수량 100 · 1인 제한 100 드롭을 승인 → 구매자 A가 100개 주문 → 구매자 B 주문 못 함. 가정이고 재현 안 함. 면접에서는 **설계했다**고만 말할 것

## 다음 시작: 리뷰 지적 고치기

- **대기 중인 질문 (답 전에 멈춤, 대신 고르지 말 것):** 명세의 "빈 값" 범위. ① `"username": ""` → 400 / 401 ② `"username": "   "`(공백만) → 400 / 401, 이유
- 리뷰 지적 (코드에 반영 안 함, 고친 코드를 주지 말 것):
    1. `AppUserLoginRequestDto`의 `@NotNull`은 `""`를 통과시켜 400이 아니라 401이 될 것으로 보임 (코드로 따짐, 실행 안 함). 어노테이션을 바꾸면 `GlobalExceptionHandler.toReason` 번역표도 볼 것 (없으면 `INVALID_VALUE` + WARN)
    2. `findAppUserByUsername`이 `AppUser`를 돌려줘 없으면 `null`. 서비스에서 확인을 빠뜨려도 컴파일됨 → NPE로 500. `findById` 반환 타입과 비교
    3. `LOGIN_FAILED` 줄 쉼표 뒤 공백이 윗줄과 다름
- 그다음 구현 4번 서비스 → 5번 컨트롤러 (`POST /login` 204) → 6번 테스트 (성공 204 + `Set-Cookie` · 없는 username 401 · 틀린 비밀번호 401 · 빈 값 400)
- 흐름 (사용자 답 통과): username으로 찾기 → 없으면 `LOGIN_FAILED` → `matches(입력값, 해시)` → 틀리면 `LOGIN_FAILED` → 세션에 회원 `id`
- 이번 범위에서 뺌: Origin 필터 · `SameSite=Lax` · 로그인 확인 필터/인터셉터 · 관리자 칸
- 리뷰 때 볼 것 (AI가 약속함): 이미 세션이 있는 상태에서 다시 로그인하는 경우

## 노션 (11/02 관리자 구분 반영)

- 기술 선택 기록: https://app.notion.com/p/40c3fff1d30881859510ce7e0b915b60
- 에러 코드 `ADMIN_REQUIRED`: https://app.notion.com/p/40c3fff1d308813eac99e22671cdd2f9 (F-14 · F-15 · F-16 · F-32 · F-37에 연결)
- [복습] 하위 페이지 5 · 6과 Q11~Q14: https://app.notion.com/p/40b3fff1d30881b0a937d3569dfd9313
- 그 밖에: 로드맵 F-12 · 회고 3주차 7일차 · API 명세 3장 체크리스트
- 3주 회고는 아직 안 함

## 이번 세션 습관

- 틀린 답: 일반 회원 호출에 401 / "쿠키에 세션을 보낸다" (쿠키에는 세션 ID) / 세션에 역할이 있다고 가정 / 서비스 반환 "없음"
- 이유: 대가의 인과 반대 / B에도 해당하는 이유 / "악용" · "리스크" 일반론 / 재고 검사가 막는 장면을 권한 손해로 답함
- 칸 빠짐: "틀리면" 칸 / 이유 칸에 다른 칸 내용
- `AM`: DTO가 빈 record로 스테이징된 채 수정함
- 잘한 것: 401 → 403을 "다시 로그인해도 안 됨"으로 스스로 구분 / 방법 가 · 나 차이 정확 / `matches` 인자 순서 / 이유를 모르면 모른다고 말함
- AI가 고칠 것: 한 문장을 네 번 고치게 해서 사용자가 "알아서 해라"고 함. 작은 칸은 짚고 넘어갈 것 / 거절 사유 없는 장면으로 물어 사용자가 장면을 지어내게 함

---

# (지난 기록) 세션 이어가기 메모 (11/02 CSRF 선택 후)

- 레포: `main` 최신 (`75c3a8c`, PR #13). 코드 변경 없음. 브랜치 `feat/user-login`은 아직 안 만듦
- 로드맵: F-12 "진행 중" (https://app.notion.com/3fe3fff1d30881a1b886c3f6e3437228)
- 복습 질문: 11/01 사용자 결정대로 먼저 내지 말 것

## CSRF에서 정한 것 (다시 묻지 말 것)

| 항목 | 결정 | 사용자 근거 |
|---|---|---|
| 방법 | B. Origin 검사 + `SameSite=Lax` | 목표가 동시성 · 조회 성능이라 Spring Security 도입과 토큰 전달 설계 비용을 안 받음. A보다 B인 이유는 AI 보완 (설정을 조금 더해 층이 둘, k6 영향 없음) |
| `Origin` 없음 · 허용 목록에 없음 | 403 `ORIGIN_NOT_ALLOWED` | 없을 때 통과시키면 위조 주문으로 구매자 A · B가 주문 기회를 잃음. 401이면 다시 로그인해도 `Origin` 누락이 그대로라 또 거부됨 |
| 적용 대상 | 상태를 바꾸는 모든 요청 (`POST /login` · `POST /users` 포함) | 로그인 전에도 상태가 바뀜 |
| 검사 순서 | Origin 검사 → 로그인 확인 | 범위가 넓은 검사를 앞에 둠. 반대 순서도 구현은 가능 |
| 한계 | 토큰 구성과 달리 브라우저 쿠키 규칙 · `Origin`에 의존. 옛 브라우저 정상 요청도 거부. k6에서 `Origin`을 빠뜨리면 거부 | |

- code 이름은 사용자 위임으로 AI가 지음. 처음 Origin 검사만 골랐을 때 AI가 문서 확인 전에 "맞습니다"라고 단정했다가 OWASP를 보고 바로잡음. B는 AI 추천을 사용자가 고른 것
- 기술 선택 기록: https://app.notion.com/p/40c3fff1d30881428773f1897790d712
- 재현 안 함. 면접에서는 **설계했다**고만 말할 것

## 다음 시작: 플랫폼 관리자 구분 방법 고르기

- **대기 중인 질문 (답 전에 멈춤, 대신 고르지 말 것):** A. `app_user`에 칸 추가 / B. 관리자 테이블 분리 / C. 설정에 관리자 목록. ① 고른 방법 ② 구매자 B가 드롭 승인 API를 호출하면 서버가 ___를 확인해 ___로 끝난다 ③ 받아들이는 대가 한 문장. 표는 로드맵 F-12 페이지
- 사실: `app_user`는 `id` · `username` · `password`뿐. 명세서 여정에 관리자 가입 장면 없음. 관리자 수 안 정해짐. 판매자는 `store.owner_app_user_id`로 구분. 관리자 URL은 `/admin/...`에 권한 확인을 한 곳에서 적용 (API 명세 3장)
- 그다음 순서: `feat/user-login`에서 로그인 구현 → 필터 또는 인터셉터 → 인가
- 구현 때 `확인할 것` (아래 11/02 목록에 더해서):
    - `Set-Cookie`에 `SameSite=Lax`가 나오는지, 위조 요청이 실제로 막히는지
    - Origin 필터를 넣은 뒤 기존 `AppUserRegisterTest` · k6 · Swagger 호출에 `Origin`을 어떻게 넣어야 하는지
    - 옛 브라우저 범위와 브라우저별 세부 동작
    - 다른 곳에 403을 쓸지는 인가 단계에서 정함 (`STORE_NOT_FOUND`는 이미 404)

## 이번 세션 습관

- 이유가 결론 반복: "토큰이 없기 때문에" · "거부하기 때문에 거부" · 한계를 "토큰을 사용하지 않는다"로 두 번
- 상황 섞임: `Origin` 없음과 허용되지 않음. 허용 목록에 이용자가 있다고 답함 (실제는 출처)
- 잘한 것: 모르면 바로 말함 · 질문 문장이 이상하면 되물음 · "Origin이 일반적인가"를 스스로 물어 AI 단정을 바로잡게 함
- AI가 고칠 것: 표준 여부는 문서 확인 후 답하기 · 근거가 약한 답을 유도하는 힌트 주지 않기 · 칸 문장을 구체적으로

## 노션 (11/02 CSRF 반영)

- 회고 3주차 7일차 · CSRF 하위 페이지 · [복습] Q8~Q10 · 에러 코드 `ORIGIN_NOT_ALLOWED`(HTTP 옵션에 403 추가) · API 명세 F-12 · 3장 체크리스트 · 세션 vs JWT 확인할 것

## 그 밖에 남은 것

- 3주 회고 (11/02 계획, 아직 안 함)
- 사용자 결정 대기: 로컬 옛 브랜치 `feat/signup-error-response` 삭제

---

# (지난 기록) 세션 이어가기 메모 (11/02 명세 정리)

> 새 세션의 AI에게: 아래 11/01 이하 내용은 **지난 기록**이다. 다시 묻지 말 것.
> plan.md는 git에서 빠져 있음 (로컬 전용, `.git/info/exclude`). 커밋하지 말 것. 기준은 노션.

- 레포: `main` 최신 (`75c3a8c`, PR #13). 이번 세션은 명세만 해서 코드 변경 없음. 브랜치 `feat/user-login`은 아직 안 만듦
- 로드맵: F-12 "진행 중" (https://app.notion.com/3fe3fff1d30881a1b886c3f6e3437228)
- 복습 질문: 11/01 사용자 결정대로 복습은 마무리 상태. 먼저 내지 말고 F-12부터 이어 갈 것

## F-12 명세에서 정한 것 (다시 묻지 말 것)

| 항목 | 결정 | 사용자 근거 |
|---|---|---|
| 요청 | `POST /login`, 본문 `username` · `password` | 세션이라는 서버 상태가 새로 생김 (AI 설명 후 정리) |
| 성공 | 204, 본문 없음, 응답 헤더 `Set-Cookie`로 세션 ID. 쿠키 이름은 기본값 `JSESSIONID` | 화면이 응답으로 할 일이 없음 |
| `/login` vs `/sessions` | A. `POST /login` 204 | 로그아웃 · 기기 목록이 명세서에 없어 세션을 다루는 API가 필요 없음 → 세션을 상태로 봄 |
| 없는 username · 틀린 비밀번호 | 같은 응답. 401 `LOGIN_FAILED` | 다르면 공격자가 가입 여부를 알아내 존재하는 계정에 비밀번호 공격 집중 |
| 입력 검증 | 빈 값만 400 `INVALID_INPUT`. F-11 길이 · 문자 규칙 적용 안 함 → `ab`는 없는 회원이라 401 | 회원가입에서 이미 적용됨, 규칙 변경 시 문제 |
| 쿠키 없음 · 세션 없음 | 둘 다 401 `LOGIN_REQUIRED` | 서버가 요청한 사용자가 누구인지 알 수 없음 |
| 로그인 실패와 code 구분 | 다르게 | 화면이 할 일이 다름 (비밀번호 안내 vs 다시 로그인) |
| 회원가입 409 | 유지. 가입 여부가 드러나는 건 대가로 받아들임 | 없애면 구매자 B가 아이디를 바꿔 가입하지 못하고 드롭을 놓침 |

- code 이름 `LOGIN_FAILED` · `LOGIN_REQUIRED`는 사용자 위임으로 AI가 지음 (처음 사용자 안 `APP_USER_AUTHENTICATION_FAILED`는 세션 없는 경우와 구분이 안 돼서 바꿈)
- "로그인"을 명세서 0장 용어집에 추가함

## 다음 시작: CSRF 방법 고르기

- **대기 중인 질문 (답 전에 멈춤, 대신 고르지 말 것):** 설명은 끝남 (SameSite 쿠키 · CSRF 토큰 · Origin 검사). ① 고른 방법 ② 요청 ②가 막히는 과정 "요청 ②는 ___ 때문에 ___로 끝난다" ③ 이 프로젝트에서 받아들이는 대가 한 문장
- 그다음 순서: 플랫폼 관리자 구분 방법 (`app_user`에 칸 없음) → `feat/user-login`에서 로그인 구현 → 필터 또는 인터셉터 → 인가
- 구현 때 `확인할 것`:
    - 없는 username이면 해시 비교를 건너뛰어 응답 시간으로 구분되는지 (측정 안 함)
    - F-11 길이 규칙이 없을 때 72바이트 넘는 비밀번호를 `matches`하면 어떻게 되는지 (`encode`는 10/28에 예외 재현, `matches`는 확인 안 함)
    - 실제 쿠키 이름이 `JSESSIONID`인지 응답 헤더로
    - `SameSite` 동작과 "JSON만 받으면 폼 전송이 막히는지"는 재현 안 함

## 이번 세션 습관

- 틀린 답: 성공 코드 "없음" · "세션에 쿠키를 담는다" (관계 반대) · 빈 값 401 · `ab` 409 · 회원가입 409 질문에 로그인 상황으로 답함 · CSRF 손해를 "금전적 손해"로 (주문 등록은 결제 전)
- 이유가 결론 반복: "B에 적합하기 때문에" · "돌려줄 본문이 없어서 204"
- 잘한 것: 모르는 건 모른다고 말함 · 409의 가입 여부 노출을 설명 듣고 스스로 연결 · 구매자 A의 1인당 구매 제한 · B의 남은 수량 원인을 정확히 짚음
- AI가 고친 것: "두 응답이 같으면 공격자는 알 수 없다"고 했다가 F-11 409로 새는 것을 바로잡음

## 노션 (11/02 반영)

- [복습] 11/01~11/02 + 개념 하위 페이지 4개: https://app.notion.com/p/40b3fff1d30881b0a937d3569dfd9313
- 회고 3주차 6일차: https://app.notion.com/p/40b3fff1d3088179b31ee2f231403813
- F-12 API 명세: https://app.notion.com/p/4003fff1d308812e9b18e6fdf90f1505
- 그 밖에 반영: 에러 코드 2개(HTTP 옵션에 401 추가) · API 명세 3장 체크리스트 · 명세서 0장 "로그인" · 기술 선택 기록 2개 · 세션 vs JWT 확인할 것

## 그 밖에 남은 것

- 3주 회고 (11/02 계획, 아직 안 함)
- 사용자 결정 대기: 로컬 옛 브랜치 `feat/signup-error-response` 삭제
- 남겨 둔 복습: 주제 4의 묶인 질문 · 10/23 Q8 · 주제 6~20

---

# (지난 기록) 세션 이어가기 메모 (11/01 정리)

> 새 세션의 AI에게: 아래 10/30 이하 내용은 **지난 기록**이다. 다시 묻지 말 것.
> plan.md는 git에서 빠져 있음 (로컬 전용, `.git/info/exclude`, 이 PC도 11/01에 적용). 커밋하지 말 것. 기준은 노션.

- 레포: `main` 최신 (`75c3a8c`, PR #13). 컴파일 통과, 테스트는 실행 안 함
- 로드맵: "진행 중" 없음. F-11 완료, F-12는 11/03 시작
- 복습 통과 (10/31~11/01): 10/30 Q7 · 10/20 Q1 · Q2
- 주제 4 **진행 중**: 대표 10/20 Q8 · 꼬리질문 TZ ② 통과, 묶인 질문 확인 남음 (후보: 10/29 Q4 · Q5, 아직 안 고름)
- 주제 5 **진행 중**: 10/20 Q1 · Q2 · Q3 통과. 10/23 Q8은 질문만 내고 멈춤
- **복습 중단 (11/01 사용자 결정):** "복습은 마무리하고 그다음 구현으로". 주제 4 · 5 마무리와 6~20은 안 함. 다시 묻지 말 것
- **다음 시작:** F-12 로그인 (11/03 계획을 11/01로 당김. 사용자 결정). API 명세가 전부 빈칸이라 명세부터 (https://app.notion.com/p/4003fff1d308812e9b18e6fdf90f1505)
    - 지금 코드: `findByUsername` 없음 · 401 에러 코드 없음 · 의존성은 `spring-security-crypto`만 (필터 체인 없음)
    - 아직 못 정한 것: 플랫폼 관리자 구분 방법 (`app_user`에 칸 없음) · CSRF 확인 (기술 선택 기록)
    - 로드맵 F-12는 "진행 중"으로 바꿈. 대기 중인 질문과 상태는 로드맵 F-12 페이지에도 있음 (https://app.notion.com/3fe3fff1d30881a1b886c3f6e3437228). 코드 · 브랜치는 아직 없음
    - **대기 중인 질문 (답 전에 멈춤, 답을 먼저 말하지 말 것):** F-11은 `POST /users`에 `201`, 본문 없음. 로그인은? ① Method · URL (이유: 서버에서 무엇이 새로 생기거나 바뀌나) ② 성공 코드와 본문 (이유) ③ 다음 요청부터 구매자 A임을 알 수 있게 서버가 응답의 ___ 에 ___ 를 담아 보낸다. 실패 응답은 다음 질문
    - 진행 순서 (AI 제안): 명세 → `feat/user-login`에서 로그인 구현 → 필터 또는 인터셉터 → 인가
- 사용자 결정 대기: 로컬 옛 브랜치 `feat/signup-error-response` 삭제 (`plan.md` 내용은 이 파일에 있음)
- 노션: [복습] 10/31~11/01 https://app.notion.com/p/40a3fff1d30881d0af83d16c73536d1d · 회고 3주차 5일차 https://app.notion.com/p/40a3fff1d30881d8b9d8eab259900048

---

# (지난 기록) 세션 이어가기 메모 (10/30 끝)

> 새 세션의 AI에게: 아래 10/29 이하 내용은 **지난 기록**이다. 다시 묻지 말 것.
> plan.md는 10/30부터 git에서 빠짐 (로컬 전용, `.git/info/exclude`). 기준은 노션.

- 회원·인증 ① (F-11) **완료** — PR #13 merge commit `75c3a8c`. 브랜치는 `main`뿐
- 주제별 복습 주제 3 **끝** (10/20 Q9 · Q10 · Q11 · Q13 통과)
- 주제 4 (Clock · @CreationTimestamp · 시간대) **진행 중** — 10/20 Q8 **통과** (노션 체크함). 기록은 노션 [복습] 10/30 5장
    - ② 시간대: 서버 UTC / @CreationTimestamp는 JVM 기본 시간대 / Clock 빈은 Asia/Seoul 고정 / 9시간 / A 20:00 주문 → created_at 11:00 → `createdAt.plusMinutes(10)`(11:10) vs `now(clock)`(20:00) → 만료 취소 → 남은 수량으로 돌아가 B가 삼 ✅
    - 짚은 것: "JVM 시간" → **JVM 기본 시간대** / 용어 "재고로 복구" → **남은 수량**
- **다음 시작 (집):** 꼬리질문 "Clock 빈 대신 서버 TZ=Asia/Seoul로 맞추면?" ②칸 다시 풀기 (노션 [복습] 10/30 Q7, 미통과)
    - ① 통과: TZ 맞춰도 now()는 고정 불가 → 테스트 못 함
    - ② 틀림: "TZ 빠뜨리면 created_at과 결제 기한이 9시간 어긋남" → Clock 없이 TZ에만 기대면 셋 다 JVM(UTC) 기준이라 서로 비교는 정상. 힌트 줌: **JVM 시계에서 오지 않은 시각**(사람이 한국 시각으로 입력한 드롭 시각)
    - 다시 채울 칸: JVM에서 오지 않은 시각(누가 어떤 값) / 저장된 값 ___ vs now ___ / 드롭이 ___ (누가 어떤 손해). 답을 먼저 말하지 말 것
    - 그다음 주제 4의 나머지 묶인 질문 → 주제 5 (주제 표는 노션 [복습] 10/29 6장)
- 일정 (10/30 사용자 결정): 10/31 · 11/01 주제 4~20 복습 / 11/02 3주 회고 + F-12 명세 읽기(코드 없음) / 11/03 F-12 시작
- 노션: [복습] 10/30 https://app.notion.com/p/4093fff1d30881b3b723daa4251926f3 · 회고 3주차 4일차 https://app.notion.com/p/4093fff1d308810bb0aedc675e36bbfa

---

# 세션 이어가기 메모 (10/29 오후 끝 → 다음 세션)

> 새 세션의 AI에게: plan.md만 보지 말고 노션 🔁 복습 노트의 **맨 아래 [복습] 페이지**부터 확인할 것. 10/29 오후에 plan.md만 보고 이미 끝낸 리마인드·통과한 질문을 다시 물었음.

## 10/29 오후에 한 것 — 밀린 복습 · 주제별 점검

- 리마인드(주차별 흐름 · Flyway · Testcontainers · Clock · Compose)는 10/29 오전에 이미 함 → [복습] 10/29 페이지
- 날짜 순서로 미통과 질문을 풀다가, 전체를 세어 보니 **미통과 약 133개** (10/27 밤 · 10/28 · 10/28 밤은 이 메모 숫자로 셈) → 같은 개념이 날마다 반복돼서 **20개 주제로 묶어 점검**하기로 함 (사용자: "면접 직전이 아니라 중간 점검 겸")
- 방식: 주제마다 대표 질문 1개 + 꼬리질문 1~2개, 통과하면 묶인 질문을 노션에서 한꺼번에 체크 + "통과 (10/29 주제 복습)"
- 통과 16개: 10/18 Q27 · 10/19 Q39 · Q41 · Q42 (10/19 미통과 없음) · 10/20 엔티티 Q1~Q7 · 10/22 밤 Q1~Q4
- 주제 1 (FK 객체 vs Long · 양방향 · 프록시 N+1) ✅ / 주제 2 (INSERT 틀 · DEFAULT · 자바 기본값 · Generated Column) ✅. 10/23 Q8(에러 1364)은 주제 5로 옮김
- 주제 표 전체와 습관은 노션 [복습] 10/29 6장, 회고 "3주차 3일차" 6장
- 습관: **비슷한 두 사건을 하나로 섞음** (Q39 ③ · Q42, 새로 보임) / 결론만 ("CHECK 둠", "로그 켜서 봄") / 빈칸에 가까운 단어 / 용어(타입↔상수, 상품 조회↔판매점 조회) / 파일 보고 답함
- `AppUserRegisterTest`의 빈 `saveNullPasswordAppUser()`는 여전히 커밋 안 함

## 다음 시작할 곳 (10/29 오후 이후)

1. **주제 3 · 10/20 Q9** (질문 이미 냄, 답 전에 멈춤): 남은 수량을 생성자 파라미터로 안 받은 이유 — ① 받으면 가능한 실수와 누가 어떤 손해(숫자 예) ② 그 줄을 빠뜨리면 들어가는 값과 에러 없이 저장되는 이유. 주제 3에는 Q10 · Q11 · Q13도 묶임
2. 주제 4~20 (순서는 노션 [복습] 10/29 6장 표)
3. 그다음 코드: `saveNullPasswordAppUser` → `getCode()` null 결정 → PR 전 결정 2개 → PR

---

# (이전) 10/29 새벽까지의 메모

## 10/29 새벽에 한 것 (복습 건너뜀, 사용자 요청)

- **`getConstraintName()` 실험 끝남** → `da0bbcb` `ConstraintNameExperimentTest` (`@Disabled("실험 기록...")`, assert 없음, 사용자 결정으로 커밋 · push)
    - 가설: e = DIVE ✅ · cause = Hibernate `ConstraintViolationException` ✅ · name = `null` ❌
    - 실제 출력 (AI가 gradle로 실행): `e = ...DataIntegrityViolationException` / `cause = org.hibernate.exception.ConstraintViolationException` / `name = [password]`
    - DB 로그 `ErrorPacket : Error: 1048-23000: Column 'password' cannot be null` → Hibernate가 미리 막지 않고 DB까지 감 (볼 것 ① 해결)
    - name이 `"password"`인 이유: `hibernate-core 7.4.5` `MySQLDialect.java` 1008~1016줄 — 에러 1048이면 `extractUsingTemplate("Column '", "'", 메시지)`로 **컬럼 이름**을 잘라 냄 (소스 읽어서 확인). 1062는 `" for key '"` 뒤를 잘라 `uk_app_user_username`
    - ⚠️ 10/28 밤 AI 설명 "NOT NULL 메시지엔 이름 없음 → null"은 **틀렸음**. `if`가 막는 이유는 "null이라서"가 아니라 "`uk_app_user_username`과 달라서(`equals` false)". 결과(`throw e`)는 같음
    - 사용자 답 (통과): "Hibernate가 DB 메시지 보고 'password' 꺼냈다" / username null이면 name = username, if 통과 못 함, DIVE
    - 덧붙인 것: `contains("username")`으로 비교했다면 username NOT NULL 버그가 409로 둔갑 (코드로 따진 결론, 재현 안 함)
    - 면접 빈칸 답 통과: "password를 null로 두고 서비스를 직접 불러서 확인 → 제약 이름 password, uk_app_user_username과 달라 원래 예외 그대로"
    - 사용자 깨달음: "예외가 하나가 아니라 예외 객체 안에 예외 객체가 있고 계속 꺼내 가는 과정" (cause 체인)
- **코드 정리 커밋** (전체 테스트 9개 통과 + 실험 1개 skipped 확인, push 함 — 222a42e는 push 전일 수 있으니 `git status -sb` 확인)
    - `536ef5e` static import 와일드카드 → `USERNAME_DUPLICATED` (사용자 이유: 상수가 늘면 어떤 걸 쓰는지 모름. AI 덧붙임: 다른 enum 와일드카드와 이름 겹치면 모호성 컴파일 에러)
    - `1cecb54` 상수를 인스턴스 필드 위로 (AI가 옮김. 이유는 관례·가독성이고 동작은 같음. 사용자: static 1개 / 인스턴스 필드 3개 ✅)
    - `c0af62d` `CommonErrorCode` 끝 빈 줄 4개 · `AppUserService` import 빈 줄 1개
    - `71e3190` `handle` → `handleGlobalException` (사용자: "섞기" — 400은 `handleInvalidInput`이 뜻이 더 잘 읽혀서. 이름 바꿔도 안 깨지는 이유: Spring이 `@ExceptionHandler` 타입으로 찾음 ✅)
    - `54f4705` `toReason` 닫는 괄호 들여쓰기
    - `222a42e` `toReason` default WARN 로그 (사용자: 남김, "번역표 빠진 걸 로그 보고 알 수 있어서" → "언제" 빠짐: 새 제약 붙이고 요청 보낼 때 콘솔). 구현은 사용자가 "알아서 해" → AI가 함 (`@Slf4j`, `default -> { log.warn(...); yield ...; }`)
        - 확인할 것: WARN이 실제로 찍히는지는 실행 안 함 (번역표에 없는 어노테이션으로 요청 보내 보기)
        - 사용자에게 아직 안 물은 것: 블록 안에서 `return` 대신 `yield`를 쓰는 이유
- 이번 세션 습관: name 칸에 예외 클래스를 씀 (칸 vs 값 혼동) · 서비스 직접 호출인데 400 `errors`에서 왔다고 함 (실험 조건 안 맞춤, 10/28 밤과 같음) · "다 고쳤어" 뒤에 `ㅁ` · import · `AM` 남음 (git diff 안 봄) · 답 칸 "언제" 빠짐
- 사용자 피드백: 면접 질문 4칸은 "너무 복잡" → 빈칸 채우기 2~3칸으로 하니 바로 답함

## 다음 시작할 곳 (10/29 이후)

> (10/29 사용자 결정) ①을 10/30에 끝내고, 남는 10/31~11/02는 **전체 리마인드 + 처음(10/13)부터의 회고 + 밀린 복습**에 씀. 하루로 할지 사흘로 나눌지는 "일단 해보고 정하자".
> AI 제안(참고): 10/31 백지 흐름 설명 + 3주 회고 / 11/01 밀린 복습 질문 (미통과 10/25 Q3부터, 오래된 순, 답 칸 2~3개) / 11/02 남은 복습 + 모의 면접. 하루로 하면 미통과 · 면접 직결 질문 10개 정도만 골라서
> 밀린 복습 질문: 최소 24개 (10/25 Q3~Q8 · 10/27 밤 Q1~Q7 · 10/28 Q1~Q6 · 10/28 밤~10/29 Q1~Q5, 10/28 오후 페이지는 안 셈)

1. **5번 `getCode()` null 결정** — 지금 어노테이션으로 정말 null이 안 생기는지 먼저 확인
2. PR 전 결정 2개: `AppUserRegisterTest`의 `@TestPropertySource` SQL 로그 · `print()` 3개를 둘지 / `plan.md` 지울지 (AI는 결정 안 함 — 사용자 몫)
3. PR → 머지 → 브랜치 삭제
4. [x] 노션 반영 (10/29 새벽): `[복습]` 10/28 밤~10/29 + 개념 페이지 4개 (https://app.notion.com/p/4073fff1d3088113b53dd9b551611377) · 회고 "3주차 2일차 밤" (https://app.notion.com/p/4073fff1d30881ff8f32d2abb8028f0a) · 로드맵 메모·본문 섹션. F-11 페이지 어긋남 3번은 아직 안 고침 (사용자 결정 대기)

---

# (이전) 10/28 밤까지의 메모

> 새 세션의 AI에게: AGENTS.md 규칙 (힌트는 단계적으로, 질문은 한 메시지에 하나, 정답 코드는 요청할 때만)을 그대로 따를 것.
> 이 파일은 작업을 이어가기 위한 임시 메모입니다. PC를 옮겨 이어가려고 10/28에 브랜치에 커밋함 (사용자 결정). PR 전에 지울지는 그때 사용자가 정함.

## 지금 위치

- 로드맵 "진행 중": **회원·인증 ① 세션 vs JWT 비교·결정, 회원가입 (F-11)** (3주차, 기간 10/23~11/02)
    - 노션: https://app.notion.com/p/3f83fff1d30881e68d81c44353a030b1 (10/28 오후 확인할 것 작업까지 메모·본문에 반영함)
- 브랜치: `feat/signup-error-response` — origin에 push함, 로컬과 같음 (PR 아직 없음)
  - 10/28 오전: 다른 PC(SSAFY)에서 `git fetch --prune` → 브랜치 받기 → 로컬 `feat/user-signup` · `docs/one-question-per-message` `-d`로 삭제
  - 10/28 브랜치 정리: 머지된 브랜치 삭제 — GitHub `docs/one-question-per-message` · `feat/user-signup`, 로컬 `feat/schema-v1` · `test/entity-insert-default`. 이제 로컬·GitHub 모두 `main`과 이 브랜치뿐
  - 다른 PC에 지운 브랜치가 남아 있으면: `git fetch --prune` 후 `git branch -d <이름>` (머지 안 된 브랜치면 `-d`가 거부하니 그때는 멈추고 확인)
- 커밋: `5e3a1b2` 409 핸들러 · `6ec2bbc` 공백 정리 · `29debb3` ErrorCode 패키지 이동 · `019ffcf` 400 INVALID_INPUT + errors · `b6a6654` 409 통합 테스트 · `00fa2dc` · `90112a0` · `cc13214` 세션 메모(chore) · `3a98f94` 400 통합 테스트 · `a037316` 201 통합 테스트
- 10/27: 리마인드 하루 (회고 있음, 코드 작업 없음)
- 10/27 밤~10/28 새벽: 409 통합 테스트 작성·커밋 (`AppUserRegisterTest`, `b6a6654`)
- 10/28 오전: 복습 10/25 Q3 (미통과) → 400 · 201 테스트 작성·커밋 → **회원가입 테스트 3개(409 · 400 · 201) 완료**
- 10/28 오후 (복습 건너뜀, 사용자 요청): 확인할 것 정리 — 72바이트 encode(가설 틀림, 예외) · `encode(null)` · INVALID_INPUT 문구 · `field` 키 · `REMAINING_QUANTITY_INSUFFICIENT` 존재 확인. 코드 변경 없음 (실험 파일은 지우고 되돌림)
- 10/28 밤 (집, 복습 건너뜀): 열린 질문 STOCK vs REMAINING_QUANTITY 끝냄 → `getConstraintName()` 실험 가설·개념 설명까지 하고 **실험 코드는 안 짬** (세션 오류로 중단). 코드 변경 없음, 노션 반영 안 함
- **다음 시작할 곳:** ① `getConstraintName()` 실험 실행 (다음 시작점 3번, 이름·순서 정해 둠) → ② 코드 정리 (5번) → 노션 반영 (10/28 밤 내용)

### 끝난 질문: STOCK vs REMAINING_QUANTITY (10/28 밤, 통과)

- 사용자 답: STOCK = "상품의 stock보다 많은 드롭을 신청할 때" / REMAINING_QUANTITY = "드롭의 남은 한정수량이 구매하려는 양보다 작을 때"
- 짚은 것:
    - `stock`은 컬럼 이름 → 명세 용어 **창고 재고**로
    - "남은 한정수량"은 용어 섞음. **한정수량**(고정, `limitedQuantity`) ≠ **남은 수량**(주문마다 줄어듦, `remainingQuantity`). 비교 대상은 남은 수량
    - STOCK 쓰는 곳 세 군데(노션 에러 코드 DB: F-23 · F-31 · F-32) 중 F-31만 말함 → **F-23 창고 재고 −n이 창고 재고보다 클 때** 빠짐. "API 설명도 했다고 생각해" → 일부만 답함
- 이어서: 신청(F-31) 때 확인했는데 승인(F-32) 때 또 확인하는 이유
    - ① 사용자: "판매자가 재고를 줄여서 한정수량보다 작아질 수 있어" (맞음). AI 덧붙임: 같은 상품의 다른 드롭이 먼저 승인되면 창고 재고가 바뀜 (요구사항 명세서 "승인 시 창고 재고 변경" 문장 근거, 정확히 빼는지/묶는지는 **확인할 것**)
    - ② 처음엔 빠뜨림 → "판매자가 창고 재고보다 많이 팔아서 구매자 주문을 취소해야 함" → 통과. AI가 구체화: 한정수량 100 승인, 창고 재고 80이면 81~100번째 구매자 20명이 결제 후 취소당함 (가정)

### 400 · 201 테스트 (10/28 오전, 같은 클래스)

| 테스트                                   | Then                                                                                                                                       | 사용자가 정한 이유                                                                                     |
|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `requestNullUsernameAndPassword` (`{}`) | 400 · `$.code` = `INVALID_INPUT` · `hasSize(2)` · `$.errors[?(@.field == 'username')].reason`에 `hasItem("REQUIRED")` (password도 같게) | field와 reason을 한 쌍으로 검사. 순서는 Bean Validation이 `Set`으로 돌려줘서 보장 안 됨               |
| `checkPasswordAndRegisterSuccess`       | 201 · `count()` = 1 · `findAll().getFirst()` · password ≠ 입력값 · `passwordEncoder.matches(입력값, 저장값)` true                        | ≠만으로는 해시 대신 엉뚱한 값을 저장하는 버그를 못 잡음. TRUNCATE 후 요청 한 번이라 행이 하나 → id에 기대지 않음 |

- 직접 재현: 요청을 `{"password":"123"}`로 바꾸면 password 줄만 `was "OUT_OF_RANGE"`로 빨강 (쌍 검사 동작 확인)
- 직접 겪음: `findById(1L)`의 `Optional`에서 password가 안 보이자 "getter가 없다"고 판단 (실제로는 `@Getter` 있음) → 운영 코드에 `findAppUserById` 추가 → 되돌리고 테스트 안에서 해결
- 코드 제공: 400의 Then 세 줄은 "코드로 보여줘", 201의 마지막 정리(import 3줄 · `findAll().getFirst()`)는 "해줘"로 AI가 함
- 아직 결정 안 한 것: 테스트 메서드 이름 동사(`save...` / `request...` / `check...`)가 섞여 있음. 그대로 둬도 된다고 함

### 409 테스트 (10/28, `src/test/.../appuser/AppUserRegisterTest.java`)

사용자가 정한 설계:

| 칸    | 정한 것                                                                                  | 이유 (사용자 말)                                                                 |
|-------|------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| Given | `appUserRepository.save()`로 유저 A 직접 저장                                            | 전처리라서 레포지토리로 바로. 정상 가입 테스트는 빈 DB에서 시작하니 Given 불필요 |
| When  | `MockMvc`로 `POST /users` (컨트롤러·서비스 직접 호출 아님)                               | 409는 `GlobalExceptionHandler`가 만들고, `DispatcherServlet`을 거쳐야 동작       |
| Then  | ① `status().isConflict()` ② `jsonPath("$.code")` = `USERNAME_DUPLICATED` ③ `count()` = 1 | `message`는 명세(에러 코드 DB)에 문구 칸이 없어서 확인 안 함 → 바꿔도 버그 아님  |

- 테스트 클래스에 `@Transactional` **안 붙임**. 테스트 사이 정리는 `IntegrationTest.cleanUp()`(TRUNCATE)이 맡음 (사용자: "없으면 앞 테스트 행이 남아서 2가
  됨")
- 일부러 실패시키기 확인함: ①②③ 각각 기대값을 바꾸면 모두 빨강
- When의 `perform` 코드와 Then 코드는 사용자가 "코드로 보여줘"라고 해서 AI가 준 것

10/28에 직접 재현한 것 (면접에서 "겪은 일"로 말해도 됨):

| 시도                                         | 결과                                                                              | 원인 (사용자가 찾음)                                                                                                                                               |
|----------------------------------------------|-----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 컨트롤러 직접 호출 + `"test1"`               | `AppUserException`이 테스트까지 날아옴 (스택 트레이스에 `DispatcherServlet` 없음) | 테스트가 컨트롤러를 직접 불러서 `DispatcherServlet`을 안 거침 → 핸들러·`@Valid` 둘 다 동작 안 함                                                                   |
| `MockMvc` + `"test1"`                        | 400, username·password 둘 다 `OUT_OF_RANGE`                                       | `@Size` 길이 규칙 (username 8~50, password 8~64). 처음엔 username만 고치려 함                                                                                      |
| 테스트 `@Transactional` + `em.persist` Given | Then ③에서 `null identifier ... flushed after an exception`                       | 서비스 트랜잭션이 테스트 트랜잭션에 참여(REQUIRED, MockMvc는 같은 스레드) → 실패한 AppUser#2가 같은 영속성 컨텍스트에 남음 → `count()` JPQL 전 자동 flush에서 터짐 |
| `@Transactional` 제거 + `em.persist`         | `No EntityManager with actual transaction`                                        | `persist`는 트랜잭션 필요 → `repository.save()`로 바꿈 (`SimpleJpaRepository.save`에 `@Transactional`)                                                             |

- 에디터의 "Could not autowire MockMvc" 밑줄은 IntelliJ 추측. 실행하면 주입됨 (IntelliJ가 왜 그러는지는 확인 안 함)

## 다음 시작점

1. [x] 409 테스트 커밋 (`b6a6654`, 필드 정리됨. `@TestPropertySource` SQL 로그와 `print()`는 남아 있음 → PR 전에 둘지 사용자가 결정)
2. **복습 루틴** (협업 지침 2장): 체크 안 된 질문 중 오래된 것부터
    - 10/25 Q3: 10/27(보고 씀) · 10/28(② (나) 다른 장면, ③ 설명 듣고 답함) 두 번 미통과 → 같은 장면으로 세 번째
    - 그다음 10/27 밤 Q1~Q7, 10/28 Q1~Q6
    - 10/28 사용자 피드백: "필드는 너무 많이 물어본다" → 답 칸은 2~3개 이내로
3. [x] 남은 테스트: `3a98f94` `{}` → 400 · `a037316` 정상 → 201 (위 표)
    - 그때 확인할 것 3개 (10/28 오후)
        - [x] 72바이트 넘는 비밀번호 encode: 가설 "앞 72바이트만 씀" → **틀림**. 73자에서 `IllegalArgumentException: password cannot be more than 72 bytes` (직접 재현, 실험 `BCryptTest`는 지움 — DTO가 바뀌어도 안 빨개져서)
            - API로는 안 생김: password `@Pattern(\x21-\x7E)` (ASCII만, 1바이트) × `@Size(max=64)` = 최대 64바이트 (코드로 따진 결론, 요청으로 재현 안 함)
            - 깨지는 경우 (사용자 답): `@Pattern`이 ASCII 밖 문자 허용 / `@Size` max가 72 초과
        - [x] `encode(null)`: `AbstractValidatingPasswordEncoder.encode`에서 `if (rawPassword == null) return null;` (소스 읽어서 확인, 실행 안 함)
        - [ ] **다음은 여기:** `getConstraintName()` 실제 값. 사용자가 목적 확인함 — username 중복이 **아닌** 제약 위반이 `catch`의 `if`를 통과하는지
            - 방법: `IntegrationTest` 상속 테스트에서 `appUserService.register(new AppUserRegisterRequestDto("testuser1", null))` (서비스 직접 호출 → `@Valid` 우회), `catch` 첫 줄에 임시로 `e.getCause()` 클래스 · `"[" + name + "]"` 출력 → 실행 → 콘솔 두 줄 + 테스트까지 날아온 예외 이름 확인 → 끝나면 되돌리기 (커밋 안 함)
            - 이름 (10/28 밤 AI 추천, 사용자 아직 고르지 않음): 클래스 `ConstraintNameExperimentTest` / 메서드 `printConstraintNameWhenPasswordNull` (assert 없이 출력만 하는 실험이라 `check...` 대신)
            - 10/28 오후 가설: 500, `throw e`. 10/28 밤 가설: "null이라서 if 통과 못 하고 throw e로 500" → 이유 칸 빠짐 · `instanceof` 또 건너뜀 · 서비스 직접 호출이라 500(HTTP)은 안 나옴(예외가 테스트까지 옴)
            - 이유를 물으니 ① cause가 `ConstraintViolationException`인지 / ② 이름이 null인 이유 둘 다 "모르겠다" → AI가 설명 (예외 세 겹 · SQLState 23000 · 메시지 문자열에서 이름 뽑기) → 사용자 "너무 어려워서 이해가 안 됨" → 쉽게 다시 설명:
                - UNIQUE 거절 사유 `Duplicate entry 'testuser1' for key 'uk_app_user_username'` → 이름 글자 **있음** → 409
                - NOT NULL 거절 사유 `Column 'password' cannot be null` → 이름 글자 **없음** → `throw e`
                - 이름 비교가 없으면 NOT NULL 버그도 409 "이미 있는 아이디"로 둔갑
                - 사용자 빈칸 답: "제약 이름이 없으니까" (맞음)
            - 실험에서 볼 것 (AI 설명, **확인할 것**): ① cause가 정말 `ConstraintViolationException`인지 — Hibernate가 SQL 전에 null을 막으면(nullability 검사) DB까지 안 가고 다른 예외 ② 이름이 null인지 · 빈 문자열인지
            - "세 겹 · SQLState"는 실험 결과를 화면에서 보면서 다시 설명하기로 함 (한 번에 많이 설명하지 말 것)
        - 열린 것: 같은 값 `encode` 두 번 → 다름 (salt 랜덤, 사용자 답 맞음, 직접 찍어 보진 않음)
4. 10/27에 미룬 **확인할 것** 3개 (사용자가 직접 노션·코드 비교)
    - [x] ① `INVALID_INPUT` 메시지 문구: 노션 [에러 코드 › INVALID_INPUT](https://app.notion.com/p/4003fff1d30881cbb71fe5106cd095e3) vs
      `CommonErrorCode` (코드: "입력값을 확인해 주세요.")
        - (10/28 오후) 노션에 문구 없음 → **안 고침**. 사용자 결론: 무엇이 잘못됐는지는 `errors`가 알려 주므로 `message`를 자세히 할 필요 없음
        - 처음엔 "고쳐야 하지 않나?"(①과 모순, 이유 없음) → "사용자는 메시지를 보니까" → 응답 본문 보고 `errors`로 바로잡음
    - (10/28 오후) 위치: API 명세 초안 › 3장 › "입력 오류의 필드 알려주기" 토글. 예시 message `"입력값을 확인해 주세요"`(마침표만 다름) — 3장 "실패 응답 형식"에서 message는 바꿔도 되는 문구라 결론 그대로
    - (10/28 오후) 같은 페이지 4장: "null·길이 초과는 검증 누락 버그라 500" → 집에서 할 `getConstraintName()` 실험의 기대값
    - [x] ② (10/28 오후) 3장 예시 `"field": "quantity"` → 코드와 같음, 안 고침. 사용자는 처음 못 찾음
    - ② `InvalidFieldDto` 키가 `field`인지 `fields`
      인지: [API 목록 › F-11](https://app.notion.com/p/4003fff1d3088107bf87cb9fb6e56e3a) vs `InvalidFieldDto` (응답에 찍힌 건
      `field`)
    - [x] ③ `REMAINING_QUANTITY_INSUFFICIENT`가 API 명세 에러 코드에 있는지 → **있음** (에러 코드 DB, 10/21 추가 · F-42 "남은 수량 부족 → 409"). 10/27 복습에서 기억으로 말한 이름이 맞았음. 사용자는 처음 "없음"이라고 답함
    - 참고: 에러 코드 DB에는 **메시지 칸이 없음** (10/28 USERNAME_DUPLICATED 페이지로 확인). ①을 볼 때 "명세가 문구를 정하는가"부터
5. 코드 정리 (각각 따로 커밋)
    - `AppUserService` 와일드카드 static import (`AppUserErrorCode.*`) → 쓰는 상수만 / 상수 `USERNAME_UNIQUE_CONSTRAINT`를 인스턴스 필드보다
      위로
    - 409 핸들러 이름 `handle` → `handleInvalidInput`과 짝 맞추기 (사용자 결정 대기)
    - `toReason` default에서 WARN 로그 남길지 (사용자 결정 대기, `@Slf4j` 필요)
    - `getCode()`가 null이면 `switch`가 NPE → 막을지 (사용자 결정 대기. 지금 어노테이션으로는 안 생길 것으로 보임, 확인 안 함)
    - `CommonErrorCode` 끝의 빈 줄 여러 개
    - ⚠️ IntelliJ Reformat/Optimize Imports는 **파일 단위로만**. 10/28에 프로젝트 전체에 적용돼서 18개 파일이 바뀜 → 사용자 결정으로 전부 되돌림
      (`V1__init.sql` 공백 변경은 Flyway 체크섬을 바꿀 수 있음, AGENTS.md 목록이 깨짐)
6. PR: `feat/signup-error-response` → main (plan.md를 PR 전에 지울지 결정). 머지 후 GitHub·로컬 브랜치 삭제 (GitHub Flow)
7. [x] 노션 반영 (10/28 새벽): 로드맵 F-11 메모 · 본문 "10/28 — 다음에 할 것" 섹션, 회고 "3주차 1일차 밤"(날짜 10/27), `[복습]` 10/27 밤 + 개념 페이지 4개 (아래 링크)
    - 10/27 밤 `[복습]` 질문 Q1~Q7은 아직 안 풂 → 다음 대화부터 복습 루틴에 포함
    - 모의 면접은 10/27에 미룸
8. [x] 노션 반영 (10/28 오전): 로드맵 메모 · 본문 "10/28 오전 — 끝난 것과 다음 순서", 회고 "3주차 2일차", `[복습]` 10/28 + 개념 페이지 4개, 10/25 Q3에
   "복습 (10/28) — 미통과" 기록

## 정한 것 (다시 묻지 말 것)

- 실패 본문은 `{code, message}`, 상태 코드는 상태 줄에만
- 핸들러는 `GlobalException` 타입으로 받음 (`RuntimeException` 아님)
- reason 번역은 `getCode()`(어노테이션 이름) 기준, 기본 메시지 아님. 표에 없으면 `INVALID_VALUE` (선택지 A, 대가: 개발자가 모름)
- `reason`은 enum, `errors`는 List, 같은 field 여러 번 허용 (선택지 A) → API 명세에 한 줄 추가함
- 400 본문은 전용 클래스 따로 (409에 `errors` 칸 없음)
- `INVALID_INPUT`은 `global/exception/errorcode/CommonErrorCode`
- (10/28) 테스트에서 HTTP는 `MockMvc`, Given은 레포지토리, 테스트 클래스에 `@Transactional` 없음, `message`는 검사 안 함
- (10/28 오전) `errors`는 `errors[0]`이 아니라 JsonPath 필터로 field·reason 쌍 검사 + `hasSize`
- (10/28 오전) 정상 가입 검증은 `matches()`까지. 저장된 행은 `count()` 확인 후 `findAll().getFirst()` (id에 기대지 않음, 테스트용 메서드를 운영 코드에 추가하지 않음)
- (10/28) `.mcp.json`은 민감한 값 없음 (이 PC의 IntelliJ MCP 주소 `127.0.0.1:64342`뿐). 다른 PC에서 포트가 다르면 고쳐 쓸 것

## 확인할 것 (실행해 보지 않음)

- `new GlobalException(null)`이 어디서 터지는지 (가설: 생성자의 `errorCode.getMessage()` 줄 NPE)
- 기본 검증 메시지 언어가 서버 JVM 설정을 따르는지, 요청 `Accept-Language`를 따르는지
- `29debb3`만 꺼내서 빌드되는지 (diff상 import 한 줄이라 될 것으로 보임)
- 동시 가입 두 스레드 → 201 + 409
- Hibernate Validator 내부가 어떤 `Set`(`HashSet` 등)을 쓰는지 / 이 프로젝트에서 `errors` 순서가 바뀌는 걸 재현한 적은 없음
- 201 테스트의 일부러 실패시키기: 사용자가 "했다"고 함, AI는 결과를 보지 못함
- `V1__init.sql` 공백만 바꿔도 Flyway `validate`가 실패하는지 (AI 설명: 줄바꿈만 무시하고 공백은 체크섬에 반영 — 확실하지 않음)

## 반복된 습관 (다음 세션에서 AI가 볼 것)

- 질문 일부만 답함 / 답 칸 빠뜨림 (10/28에도: Given "어떻게 만드나", 가설 칸, 스택 트레이스 2·3번)
- 이유를 일반론으로, 또는 소거법으로 ("모르니까 후자")
- "값을 가진 것"과 "응답을 만드는 것" 혼동: 10/28에 409를 만드는 곳을 `AppUserErrorCode`라고 답함 (실제는 `GlobalExceptionHandler`). 10/27 회고의 "예외
  클래스 vs 에러 코드"와 같은 모양
- 조건부 결론을 조건 확인 없이 냄 ("명세에 문구가 있으면 확인함" → 명세엔 메시지 칸이 없었음)
- 코드 붙여 넣은 뒤 정리 안 함 (남은 필드·import). 10/28에 "정리 다 했어" 했는데 필드 3개가 남아 있었음
- 파일이 `AM`인 채로 커밋하려 함 → 커밋 전 디스크 내용·스테이징 다시 확인
- (10/28 오전) "다 고쳤는데 확인해봐" 뒤에 할 일이 남음: 주석 모양 · 기존 테스트 변경 / `findById(1L)` 그대로 · 운영 코드 빈 줄 삭제 · import 3개 → "확인해봐" 전에 `git status` · `git diff`를 직접 보게 하기
- (10/28 오전) 원인을 확인 안 하고 고침: "getter가 없어서" → 실제는 `Optional` → 운영 코드에 메서드 추가로 이어짐
- (10/28 오전) 선택은 맞는데 이유를 가까운 개념으로: "영속성 컨텍스트에 있으니까" (실제: TRUNCATE 후 행이 하나)
- 용어: `{}`(칸 없음, null)를 blank라고 부름
- (10/28 오후) 근거 없이 들은 것으로 가설 ("들었던 것 같다") → 틀림. 뒤에 `encode(null)`은 인터페이스 선언을 찾아와서 나아짐
- (10/28 오후) 경계값: "72바이트 이상" (실제는 초과부터 예외) — ①·③에서 두 번
- (10/28 오후) 실험 입력을 엉뚱한 칸에 넣고 결과를 근거로 씀 (한글을 password 대신 username에) → 면접 답 ①에 하지 않은 실험이 들어감. 사용자는 "말장난"이라며 넘김
- (10/28 오후) username 규칙(50, a-z0-9)과 password 규칙(64, \x21-\x7E)을 섞음
- (10/28 오후) `matches`가 "DB에 저장된 값과 비교"한다고 함 (실제: 인자로 받은 문자열과 비교) — 값을 가진 것 / 일을 하는 것 혼동
- (10/28 밤) 명세 용어 섞음: `stock`(컬럼 이름) · "남은 한정수량"(한정수량 + 남은 수량)
- (10/28 밤) 답 칸 빠뜨림 두 번: STOCK 사용처 1/3만 · 손해 칸 ② 통째로 · 가설의 이유 칸
- (10/28 밤) `&&` 앞 조건(`instanceof`) 건너뜀 — 10/28 오후 지적 후에도 반복
- (10/28 밤) 실험 조건과 결과를 안 맞춤: 서비스 직접 호출인데 "500"
- (10/28 밤) AI 쪽 문제: 개념 설명을 한 번에 너무 많이(표 + 3단계) 해서 "너무 어렵다" → 한 문장 + 실제 에러 메시지 두 줄 비교로 다시 하니 답함. 설명은 짧게 쪼갤 것

## 노션에서 발견한 어긋남 (아직 안 고침, 사용자에게 고칠지 물어봄)

1. 기술 선택 기록 「주문 목록의 상품명을 어디서 가져올지」가 "Flyway V4"로 적혀 있음 → 실제로 V4는 username · password가 썼고, API 명세 · ERD는 "V5 이후"
2. ERD 페이지의 드롭 (product_drop) 컬럼 표에 `approved_at` 행이 없음 (Mermaid 그림에만 있음)
3. (10/28 오후) F-11 페이지 요청 섹션: "한글 24자를 넘으면 뒤가 무시됨" → 실제로는 72바이트 넘으면 `IllegalArgumentException` (직접 재현). 또 "`확인할 것`: 72바이트 넘는 비밀번호 encode" 항목 결과 반영 필요

## 링크

- 10/28 `[복습]`: https://app.notion.com/p/4073fff1d30881b5b652d270bc0891f2
- 10/28 회고 (3주차 2일차): https://app.notion.com/p/4073fff1d30881e59311db3bf15cb294
- 10/27 밤 `[복습]`: https://app.notion.com/p/4063fff1d308812d824cd08067b739ed
- 10/27 밤 회고: https://app.notion.com/p/4063fff1d308811ab485d337bb781b71
- 10/27 (낮) 회고: https://app.notion.com/4063fff1d30881d580c4c5502f03fc7b
- 10/25 `[복습]`: https://app.notion.com/p/4033fff1d30881c7a198cac56c564f25
- 10/25 회고: https://app.notion.com/p/4033fff1d308816b9bd1deeadd05b0b8
- 10/24 `[복습]`: https://app.notion.com/p/4023fff1d3088188b610c041878f20b7
- API 명세 (3장): https://app.notion.com/p/3f83fff1d3088177888acd3c2fca8b36
- 에러 코드 › USERNAME_DUPLICATED: https://app.notion.com/p/4023fff1d3088100b157c259d0fc93c8
- 복습 노트: https://app.notion.com/p/4013fff1d3088172bbdbedf193f9470d
- 프로젝트 메인: https://app.notion.com/p/3f83fff1d308811f9907ca77d733a39f
