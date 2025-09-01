# Spring Advanced Project

## 1. 프로젝트 소개

본 프로젝트는 Spring Boot를 활용하여 **JWT 기반 인증 시스템을 갖춘 Todo List 애플리케이션**입니다. 사용자 인증, 일정 관리, 댓글 기능 등을
포함하고 있으며, 관리자(Admin)는 사용자 및 댓글을 관리할 수 있는 권한을 가집니다.

AOP를 이용한 로깅, Filter를 사용한 전역 인증 처리, ArgumentResolver를 통한 커스텀 어노테이션 처리 등 Spring의 고급 기능을 학습하고 적용하는 것을
목표로 합니다.

## 2. 주요 기능

- **사용자 인증**:
    - 회원가입 및 로그인 (JWT 토큰 발급)
    - BCrypt를 이용한 비밀번호 암호화
    - `@Valid`를 이용한 비밀번호 유효성 검증
- **일정 관리**:
    - 일정 생성, 조회, 수정, 삭제
    - 로그인한 사용자만 자신의 일정을 관리 가능
- **댓글 관리**:
    - 일정에 대한 댓글 생성, 수정, 삭제
    - 로그인한 사용자만 댓글 작성 가능
- **관리자 기능**:
    - 사용자 목록 조회 및 권한 변경
    - 전체 댓글 목록 조회 및 삭제
    - AOP를 활용한 관리자 API 호출 로깅
- **공통 및 예외 처리**:
    - `@RestControllerAdvice`를 이용한 전역 예외 처리
    - JWT 인증 처리를 위한 `JwtFilter` 구현
    - `@Auth` 커스텀 어노테이션 및 `HandlerMethodArgumentResolver`를 통한 인증 사용자 정보 주입

## 3. 기술 스택

- **Framework**: Spring Boot 3.3.3
- **Language**: Java 17
- **Database**:
    - JPA (Spring Data JPA)
    - MySQL
- **Authentication**:
    - JWT (Java JWT - jjwt)
    - BCrypt
- **Build Tool**: Gradle
- **Others**:
    - Lombok
    - Validation

## 4. API 명세

<details>
<summary><b>인증 (Auth)</b></summary>

| Method | URI            | 설명   | 인증 필요 |
|:-------|:---------------|:-----|:------|
| `POST` | `/auth/signup` | 회원가입 | No    |
| `POST` | `/auth/signin` | 로그인  | No    |

</details>

<details>
<summary><b>일정 (Todo)</b></summary>

| Method | URI               | 설명            | 인증 필요 |
|:-------|:------------------|:--------------|:------|
| `POST` | `/todos`          | 일정 생성         | Yes   |
| `GET`  | `/todos`          | 일정 목록 조회(페이징) | No    |
| `GET`  | `/todos/{todoId}` | 일정 단건 조회      | No    |

</details>

<details>
<summary><b>댓글 (Comment)</b></summary>

| Method | URI                        | 설명       | 인증 필요 |
|:-------|:---------------------------|:---------|:------|
| `POST` | `/todos/{todoId}/comments` | 댓글 생성    | Yes   |
| `GET`  | `/todos/{todoId}/comments` | 댓글 목록 조회 | No    |

</details>

<details>
<summary><b>담당자 (Manager)</b></summary>

| Method   | URI                                    | 설명        | 인증 필요 |
|:---------|:---------------------------------------|:----------|:------|
| `POST`   | `/todos/{todoId}/managers`             | 담당자 지정    | Yes   |
| `GET`    | `/todos/{todoId}/managers`             | 담당자 목록 조회 | No    |
| `DELETE` | `/todos/{todoId}/managers/{managerId}` | 담당자 삭제    | Yes   |

</details>

<details>
<summary><b>사용자 (User)</b></summary>

| Method | URI               | 설명        | 인증 필요 |
|:-------|:------------------|:----------|:------|
| `GET`  | `/users/{userId}` | 사용자 정보 조회 | No    |
| `PUT`  | `/users`          | 비밀번호 변경   | Yes   |

</details>

<details>
<summary><b>관리자 (Admin)</b></summary>

| Method   | URI                           | 설명        | 인증 필요 (ADMIN) |
|:---------|:------------------------------|:----------|:--------------|
| `PATCH`  | `/admin/users/{userId}`       | 사용자 권한 변경 | Yes           |
| `DELETE` | `/admin/comments/{commentId}` | 댓글 삭제     | Yes           |

</details>