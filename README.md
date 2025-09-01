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
└── frontend/               # Vue.js frontend (TBD)
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

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

- **Developer**: [@griotold](https://github.com/griotold)
- **Email**: your-email@example.com

---

⭐ If you found this project helpful, please give it a star!