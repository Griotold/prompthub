# 🚀 PromptHub

> AI Prompt Sharing Platform - Share, discover and manage AI prompts for ChatGPT, Claude, and other AI tools

## 📝 Overview

PromptHub is a community-driven platform where users can share, discover, and collaborate on effective AI prompts. Whether you're using ChatGPT, Claude, Midjourney, or other AI tools, find the perfect prompt for your needs.

## ✨ Features

- 🔍 **Discover Prompts** - Browse categorized prompts by AI model and use case
- 📝 **Share Your Prompts** - Upload and share your best prompts with the community
- ❤️ **Like & Bookmark** - Save your favorite prompts for easy access
- 🏷️ **Tag System** - Organize prompts with tags for better discoverability
- 🔐 **User Authentication** - Secure login and profile management

## 🛠️ Tech Stack

<div align="center">

### Backend
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/spring boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/spring security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
<br>
<img src="https://img.shields.io/badge/postgresql-336791?style=for-the-badge&logo=postgresql&logoColor=white">
<img src="https://img.shields.io/badge/h2-1021ff?style=for-the-badge&logo=h2&logoColor=white">
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">

### Frontend
<img src="https://img.shields.io/badge/vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white">
<img src="https://img.shields.io/badge/typescript-3178C6?style=for-the-badge&logo=typescript&logoColor=white">
<img src="https://img.shields.io/badge/tailwindcss-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white">

### Infrastructure
<img src="https://img.shields.io/badge/amazonaws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
<img src="https://img.shields.io/badge/amazon ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
<img src="https://img.shields.io/badge/amazon rds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white">
<img src="https://img.shields.io/badge/vercel-000000?style=for-the-badge&logo=vercel&logoColor=white">

### Tools
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">

</div>

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### Backend Setup
```bash
# Clone repository
git clone https://github.com/griotold/prompthub.git
cd prompthub

# Start PostgreSQL with Docker
docker run --name prompthub-postgres \
  -e POSTGRES_DB=prompthub \
  -e POSTGRES_USER=spring \
  -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 -d postgres:15

# Run Spring Boot application
./gradlew bootRun
```

### Frontend Setup
```bash
# Navigate to frontend directory (will be added later)
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

## 📚 API Documentation

### Authentication
- `POST /api/auth/signup` - Member registration
- `POST /api/auth/login` - Member login

### Prompts
- `GET /api/prompts` - Get all prompts (with pagination)
- `POST /api/prompts` - Create new prompt
- `GET /api/prompts/{id}` - Get prompt by ID
- `PUT /api/prompts/{id}` - Update prompt
- `DELETE /api/prompts/{id}` - Delete prompt

### Interactions
- `POST /api/prompts/{id}/like` - Like/unlike prompt
- `POST /api/prompts/{id}/bookmark` - Bookmark/unbookmark prompt

## 🗂️ Project Structure

```
prompthub/
├── src/main/java/com/griotold/prompthub/
│   ├── domain/              # Domain entities and value objects
│   │   ├── Member.java      # Member aggregate root
│   │   ├── Email.java       # Email value object
│   │   ├── Role.java        # Member role enum
│   │   ├── MemberStatus.java # Member status enum
│   │   ├── MemberRegisterRequest.java # Registration DTO
│   │   ├── MemberDetail.java # User details for authentication
│   │   ├── PasswordEncoder.java # Password encoding interface
│   │   └── AbstractEntity.java # Base entity class
│   ├── adapter/             # Hexagonal Architecture Adapters
│   ├── application/         # Application services and use cases
│   │   └── member/          # Member-related application services
│   │       ├── provided/    # Input ports (interfaces)
│   │       └── required/    # Output ports (interfaces)
│   │           ├── MemberModifyService.java
│   │           └── MemberQueryService.java
│   └── config/              # Configuration classes
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── application-test.yml
├── src/test/java/com/griotold/prompthub/
│   └── support/             # Test utilities and annotations
│       ├── annotation/      # Custom test annotations
│       └── utils/           # Test utility classes
└── frontend/               # Vue.js frontend (TBD)
```

## 🧪 Test Annotations

This project uses custom test annotations for cleaner and more consistent testing:

- **`@ApplicationTest`** - Application/Service layer tests
  - Includes: `@SpringBootTest`, `@ActiveProfiles("test")`, `@Transactional`
- **`@IntegrationTest`** - End-to-End integration tests (Controller → Service → Repository → Database)
  - Includes: `@SpringBootTest`, `@ActiveProfiles("test")`, `@AutoConfigureMockMvc`, `@Transactional`
- **`@RepositoryTest`** - Repository/Data layer tests
  - Includes: `@DataJpaTest`, `@ActiveProfiles("test")`

### Usage Example
```java
@ApplicationTest
class MemberServiceTest {
    // Application layer test
}

@IntegrationTest  
class MemberControllerTest {
    // End-to-End integration test with MockMvc
}

@RepositoryTest
class MemberRepositoryTest {
    // JPA Repository test
}
```

## 🎯 Development Roadmap

### Phase 1: MVP (Current)
- [x] Project setup & database design
- [x] Member domain model implementation
- [ ] Member authentication system (JWT)
- [ ] Category and Prompt domain models
- [ ] Prompt CRUD operations
- [ ] Basic search and filtering
- [ ] Like and bookmark features

### Phase 2: Enhancement
- [ ] Vue.js frontend development
- [ ] Tag system implementation
- [ ] Advanced search and filters
- [ ] User profiles and settings

### Phase 3: Community Features
- [ ] Comment system
- [ ] User following system
- [ ] Prompt rating and reviews
- [ ] Premium prompt marketplace

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 📝 Commit Message Guidelines (for AI Assistant)

This project uses Korean commit messages with a specific format for better collaboration with AI assistants:

**Format:**
```
<type>: <title in Korean>

- <description 1 in Korean>
- <description 2 in Korean>
- <description 3 in Korean>
```

**Types:**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가/수정
- `docs`: 문서 수정
- `style`: 코드 스타일 수정 (포맷팅 등)
- `chore`: 빌드/설정 관련 작업

**Example:**
```
feat: 테스트 표준 어노테이션 추가

- @ApplicationTest: 서비스 계층 테스트용 표준 어노테이션 추가
- @IntegrationTest: HTTP-DB 전체 플로우 통합 테스트용 어노테이션 추가  
- @RepositoryTest: JPA 리포지토리 계층 테스트용 어노테이션 추가
```

**Requirements:**
- Title should be in Korean and describe the main change
- Use 3 bullet points to summarize key changes
- Each bullet point should be specific and actionable
- Keep descriptions concise but informative

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

- **Developer**: [@griotold](https://github.com/griotold)
- **Email**: your-email@example.com

---

⭐ If you found this project helpful, please give it a star!