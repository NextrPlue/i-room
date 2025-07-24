---
name: pr 템플릿
about: pr 날릴 때의 기본 템플릿
title: ''
labels: ''
assignees: ''

---

## PR 타입(하나 이상의 PR 타입을 선택해주세요)
- [ ] 기능 추가
- [ ] 기능 삭제
- [ ] 버그 수정
- [ ] 의존성, 환경 변수, 빌드 관련 코드 업데이트

---

## 변경 사항
- 변경 내용

---
## 테스트 결과
- 테스트 명
```
POST http://localhost:8080/workerEdu/record
Content-Type: application/json

{
  "workerId": 1,
  "name": "근로자1",
  "certUrl": "https://example.com/certificate/123.pdf",
  "eduDate": "2025-07-20"
}
```
```
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 22 Jul 2025 07:26:20 GMT

{
  "id": 1,
  "workerId": 1,
  "name": "근로자1",
  "certUrl": "https://example.com/certificate/123.pdf",
  "eduDate": "2025-07-20"
}
```
