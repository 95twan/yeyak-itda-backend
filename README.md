# **예약잇다 (Yeyak-itda) \- Backend Repository**

## **📖 프로젝트 소개**

**예약잇다**는 식당 방문 전, 사용자와 사장님 모두에게 편리한 예약 경험을 제공하는 웹 기반 예약 서비스

이 프로젝트는 단순한 CRUD 기능을 넘어, 대용량 트래픽과 동시성 이슈를 고려한 백엔드 아키텍처 설계를 목표로 합니다. 실시간 좌석 관리, Lock을 활용한 동시성 제어 등 실서비스에서 마주할 수 있는 복잡한 문제 해결에 도전하며, 유지보수성과 확장성이 뛰어난 코드를 구현하고자 합니다.

## **✨ 주요 기능**

### **사용자 기능**

* **인증**: JWT (Access/Refresh Token) 기반의 회원가입, 로그인, 로그아웃, 토큰 재발급 기능을 제공합니다.  
* **식당 조회**: 카테고리, 키워드, 테마별 식당 목록을 필터링 및 정렬하여 조회할 수 있습니다.  
* **식당 상세 정보**: 식당의 상세 정보, 메뉴, 운영 시간, 실시간 예약 가능 슬롯, 최신 리뷰 목록을 한 번에 확인할 수 있습니다.  
* **예약**: 원하는 날짜와 시간을 선택하여 실시간으로 식당을 예약하고, 마이페이지에서 예약 내역을 확인 및 취소할 수 있습니다.  
* **리뷰**: 식당 이용 후 별점, 코멘트, 사진을 포함한 리뷰를 작성할 수 있습니다.

### **점주 및 관리자 기능 (Admin)**

* **식당 관리**: 식당 정보, 메뉴, 영업시간, 예약 가능 슬롯 등을 등록하고 관리합니다.  
* **예약 관리**: 실시간으로 들어오는 예약 요청을 확인하고 상태를 관리합니다.

## **🚀 경쟁력 포인트 (Technical Highlights)**

* **안전하고 효율적인 인증 시스템**: Stateless 아키텍처를 위해 Access/Refresh Token을 분리하고, DB에 Refresh Token을 저장하여 서버에서 사용자의 로그인 상태를 제어합니다. 토큰 재발급 시 Refresh Token Rotation 전략을 적용하여 탈취된 토큰의 재사용을 방지합니다.  
* **동시성 제어**: 여러 사용자가 동시에 특정 시간대에 예약을 시도할 때 발생할 수 있는 Race Condition 문제를 해결하기 위해 데이터베이스 Lock을 활용하여 데이터 정합성을 보장합니다.  
* **성능 최적화**: QueryDSL을 도입하여 복잡하고 동적인 검색 쿼리를 효과적으로 처리하고, N+1 문제를 방지하기 위해 Fetch Join을 적극적으로 활용합니다.  
* **유연한 데이터 관리**: 정형 데이터(MySQL)와 비정형 데이터(MongoDB)의 특성을 고려하여, 식당의 기본 정보는 RDBMS에, 변경이 잦고 구조가 유연해야 하는 '운영 시간' 정보는 MongoDB에 저장하여 데이터 모델의 유연성과 확장성을 확보했습니다.  
* **자동화된 배포 파이프라인**: GitHub Actions를 활용하여 Main 브랜치에 Push가 발생하면 자동으로 테스트, 빌드, Docker 이미지 생성 및 Docker Hub 푸시, 최종적으로 서버에 배포되는 CI/CD 파이프라인을 구축하여 개발 생산성을 향상시켰습니다.

## **🛠 기술 스택**

| 구분 | 기술 |
| :---- | :---- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x, Spring Security |
| **ORM** | JPA (Hibernate), QueryDSL |
| **Database** | MySQL, MongoDB |
| **Storage** | MinIO |
| **Security** | JWT (jjwt library) |
| **Testing** | JUnit5, Mockito |
| **Build Tool** | Gradle |
| **CI/CD** | GitHub Actions, Docker, Docker Hub |

## **ERD**

<img width="1071" height="1312" alt="ERD-yeyak-itda" src="https://github.com/user-attachments/assets/273c5ff9-824e-449a-b957-d378dd068e02" />

## **📌 API 명세**

**인증 API**

| 기능 | Method | Endpoint | 설명 |
| :---- | :---- | :---- | :---- |
| 회원가입 | POST | /api/auth/register | 신규 사용자를 등록합니다. |
| 로그인 | POST | /api/auth/login | Access/Refresh Token을 발급합니다. |
| 로그아웃 | DELETE | /api/auth/logout | 서버에 저장된 Refresh Token을 삭제합니다. |
| 토큰 재발급 | POST | /api/auth/reissue | Refresh Token으로 새 토큰들을 발급합니다. |

**사용자 API**

| 기능 | Method | Endpoint | 설명 |
| :---- | :---- | :---- | :---- |
| 내 정보 조회 | GET | /api/users/me | 현재 로그인된 사용자의 정보를 조회합니다. |
| 내 예약 목록 조회 | GET | /api/users/me/reservations | 현재 로그인된 사용자의 모든 예약 내역을 조회합니다. |

**식당 API**

| 기능 | Method | Endpoint | 설명 |
| :---- | :---- | :---- | :---- |
| 식당 목록 조회 | GET | /api/restaurants | 필터링, 검색, 정렬 조건으로 식당 목록을 조회합니다. |
| 식당 상세 조회 | GET | /api/restaurants/{restaurantId} | 식당 상세 정보, 메뉴, 예약 슬롯, 리뷰를 조회합니다. |
| 예약 슬롯 조회 | GET | /api/restaurants/{restaurantId}/reservationSlots | 특정 날짜의 예약 가능 슬롯 목록을 조회합니다. |

**예약 및 리뷰 API**

| 기능 | Method | Endpoint | 설명 |
| :---- | :---- | :---- | :---- |
| 예약 생성 | POST | /api/restaurants/{restaurantId}/reservations | 특정 슬롯에 예약을 생성합니다. |
| 예약 취소 | DELETE | /api/restaurants/{restaurantId}/reservations/{reservationId} | 예약을 취소합니다. |
| 리뷰 작성 | POST | /api/restaurants/{restaurantId}/reviews | 식당에 대한 리뷰와 사진을 등록합니다. |

**이벤트 및 테마 API**

| 기능 | Method | Endpoint | 설명 |
| :---- | :---- | :---- | :---- |
| 이벤트 배너 조회 | GET | /api/events/banners | 메인 페이지에 노출될 진행 중인 이벤트 배너 목록을 조회합니다. |
| 이벤트 상세 조회 | GET | /api/events/{eventId} | 특정 이벤트의 상세 정보를 조회합니다. |
| 테마별 식당 그룹 조회 | GET | /api/restaurant-groups?group=theme | 메인 페이지 노출용 테마별 식당 그룹 목록을 조회합니다. |
