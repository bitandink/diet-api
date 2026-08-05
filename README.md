# 🍱 Diet API

Spring Boot 기반 식단 관리 REST API

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Data JPA
- H2
- MySQL
- Spring Security
- JWT
- Docker
- AWS EC2

## Architecture

Controller
↓
Service
↓
Repository
↓
Database

## API

GET /api/meals

POST /api/meals

PUT /api/meals/{id}

DELETE /api/meals/{id}

## Progress

- [x] Spring Boot 프로젝트 생성
- [x] Health Check API
- [x] 프로젝트 패키지 구조 생성
- [x] Meal Entity
- [x] Meal Repository
- [x] Meal Service
- [x] H2 Database
- [ ] CRUD API
- [ ] MySQL
- [ ] JWT
- [ ] AWS EC2 배포
