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

### Backend
- **Java 17** + **Spring Boot 3.2**
- **Spring Security** + **JWT Authentication**
- **Spring Data JPA** + **PostgreSQL**
- **AWS EC2** + **RDS**

### Frontend
- **Vue.js 3** + **TypeScript**
- **Tailwind CSS** + **Headless UI**
- **Vercel** Deployment

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
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login

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
│   ├── domain/          # Entity classes
│   ├── repository/      # Data access layer
│   ├── service/         # Business logic
│   ├── controller/      # REST API endpoints
│   └── config/          # Configuration classes
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── application-test.yml
└── frontend/           # Vue.js frontend (TBD)
```

## 🎯 Development Roadmap

### Phase 1: MVP (Current)
- [x] Project setup & database design
- [ ] User authentication system
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