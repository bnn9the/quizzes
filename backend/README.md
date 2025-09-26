# Course Platform Backend

Spring Boot REST API для платформы управления курсами и тестированием.

## 🏗️ Технологии

- **Spring Boot 3** - Основной фреймворк
- **Spring Security** - Аутентификация и авторизация
- **Spring Data JPA** - Работа с базой данных
- **PostgreSQL** - База данных
- **JWT** - Токены аутентификации
- **Flyway** - Миграции базы данных
- **Swagger/OpenAPI** - Документация API
- **MapStruct** - Маппинг объектов
- **Lombok** - Упрощение кода

## 📁 Структура проекта

```
src/main/java/edu/platform/
├── config/                 # Конфигурация приложения
│   ├── OpenApiConfig.java  # Настройка Swagger
│   └── SecurityConfig.java # Настройка безопасности
├── controller/             # REST контроллеры
│   ├── AuthController.java # Аутентификация
│   ├── CourseController.java # Управление курсами
│   ├── QuizController.java # Управление тестами
│   └── UserController.java # Управление пользователями
├── dto/                    # Data Transfer Objects
│   ├── request/           # DTO для запросов
│   └── response/          # DTO для ответов
├── entity/                # JPA сущности
│   ├── enums/            # Перечисления
│   ├── User.java         # Пользователь
│   ├── Course.java       # Курс
│   ├── Quiz.java         # Тест
│   ├── Question.java     # Вопрос
│   ├── AnswerOption.java # Вариант ответа
│   ├── QuizAttempt.java  # Попытка прохождения теста
│   └── StudentAnswer.java # Ответ студента
├── exception/             # Обработка исключений
├── mapper/               # MapStruct мапперы
├── repository/           # Spring Data репозитории
├── security/             # Компоненты безопасности
├── service/              # Бизнес-логика
└── util/                 # Утилиты
```

## 🚀 Запуск

### Предварительные требования
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

### 1. Настройка базы данных

Создайте базу данных PostgreSQL:
```sql
CREATE DATABASE course_platform;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE course_platform TO postgres;
```

### 2. Настройка приложения

Проверьте настройки в `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/course_platform
    username: postgres
    password: password
```

### 3. Запуск приложения

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: http://localhost:8080

## 📚 API Документация

После запуска приложения Swagger UI доступен по адресу:
http://localhost:8080/swagger-ui.html

### Основные эндпоинты

#### Аутентификация
- `POST /api/auth/register` - Регистрация пользователя
- `POST /api/auth/login` - Вход в систему

#### Пользователи
- `GET /api/users/me` - Информация о текущем пользователе
- `GET /api/users/{id}` - Информация о пользователе по ID

#### Курсы
- `GET /api/courses` - Список всех курсов
- `POST /api/courses` - Создание курса (TEACHER/ADMIN)
- `GET /api/courses/{id}` - Детали курса
- `PUT /api/courses/{id}` - Обновление курса (TEACHER/ADMIN)
- `DELETE /api/courses/{id}` - Удаление курса (TEACHER/ADMIN)
- `GET /api/courses/my` - Мои курсы (TEACHER/ADMIN)
- `GET /api/courses/search` - Поиск курсов

#### Тесты
- `POST /api/quizzes` - Создание теста (TEACHER/ADMIN)
- `GET /api/quizzes/course/{courseId}` - Тесты курса
- `GET /api/quizzes/{id}` - Детали теста
- `PUT /api/quizzes/{id}` - Обновление теста (TEACHER/ADMIN)
- `DELETE /api/quizzes/{id}` - Удаление теста (TEACHER/ADMIN)
- `POST /api/quizzes/{id}/start` - Начать прохождение теста (STUDENT)
- `POST /api/quizzes/submit` - Отправить ответы (STUDENT)
- `GET /api/quizzes/attempts/my` - Мои попытки (STUDENT)

## 🔐 Безопасность

### Аутентификация
- JWT токены с настраиваемым временем жизни (24 часа по умолчанию)
- Хэширование паролей с BCrypt
- Защита эндпоинтов на основе ролей

### Роли пользователей
- **STUDENT** - Просмотр курсов, прохождение тестов
- **TEACHER** - Создание курсов и тестов, управление своим контентом
- **ADMIN** - Полный доступ ко всем функциям

### Авторизация
Используйте JWT токен в заголовке:
```
Authorization: Bearer <your-jwt-token>
```

## 🗄️ База данных

### Миграции
Используется Flyway для управления миграциями. Файлы миграций находятся в:
`src/main/resources/db/migration/`

### Схема базы данных
Основные таблицы:
- `users` - Пользователи
- `courses` - Курсы
- `quizzes` - Тесты
- `questions` - Вопросы
- `answer_options` - Варианты ответов
- `quiz_attempts` - Попытки прохождения тестов
- `student_answers` - Ответы студентов

## 🧪 Тестирование

### Запуск тестов
```bash
mvn test
```

### Тестовые данные
Для тестирования создайте пользователей через API:

**Администратор:**
```json
{
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@example.com",
  "password": "admin123",
  "role": "ADMIN"
}
```

**Преподаватель:**
```json
{
  "firstName": "Teacher",
  "lastName": "User", 
  "email": "teacher@example.com",
  "password": "teacher123",
  "role": "TEACHER"
}
```

**Студент:**
```json
{
  "firstName": "Student",
  "lastName": "User",
  "email": "student@example.com", 
  "password": "student123",
  "role": "STUDENT"
}
```

## 🔧 Конфигурация

### Профили
- `default` - Разработка
- `prod` - Продакшн

### Основные настройки
```yaml
server:
  port: 8080

spring:
  security:
    jwt:
      secret: mySecretKey123456789012345678901234567890
      expiration: 86400000 # 24 часа

logging:
  level:
    edu.platform: DEBUG
```

## 📊 Мониторинг

Spring Boot Actuator эндпоинты:
- `/actuator/health` - Состояние приложения
- `/actuator/info` - Информация о приложении
- `/actuator/metrics` - Метрики

## 🚀 Деплой

### Сборка
```bash
mvn clean package
```

### Запуск JAR
```bash
java -jar -Dspring.profiles.active=prod target/course-platform-1.0.0.jar
```

## 🤝 Разработка

### Добавление новых функций
1. Создайте сущность в `entity/`
2. Добавьте репозиторий в `repository/`
3. Создайте сервис в `service/`
4. Добавьте контроллер в `controller/`
5. Создайте DTO в `dto/`
6. Добавьте маппер в `mapper/`

### Миграции БД
Создайте новый файл миграции:
```
src/main/resources/db/migration/V{version}__{description}.sql
```
