# 🎓 Course Management Platform

Полноценная платформа для управления курсами и тестированием с современным веб-интерфейсом.

## 🏗️ Архитектура

```
course-platform/
├── 📁 backend/                    # Spring Boot REST API
│   ├── src/main/java/            # Java исходники
│   ├── src/main/resources/       # Конфигурация и ресурсы
│   ├── pom.xml                   # Maven зависимости
│   └── README.md                 # Документация бэкенда
├── 📁 frontend/                  # React приложение
│   ├── src/                      # React исходники
│   ├── public/                   # Статические файлы
│   ├── package.json              # npm зависимости
│   └── README.md                 # Документация фронтенда
├── start-dev.bat                 # Скрипт запуска (Windows)
├── start-dev.sh                  # Скрипт запуска (Linux/Mac)
├── QUICK_START.md                # Быстрый старт
└── README.md                     # Этот файл
```

## 🚀 Технологический стек

### Backend
- **Spring Boot 3** - REST API фреймворк
- **Spring Security** - Аутентификация и авторизация
- **Spring Data JPA** - Работа с базой данных
- **PostgreSQL** - Реляционная база данных
- **JWT** - Токены аутентификации
- **Flyway** - Миграции базы данных
- **Swagger/OpenAPI** - Документация API

### Frontend
- **React 18** - UI библиотека
- **TypeScript** - Статическая типизация
- **Material-UI (MUI)** - Компоненты интерфейса
- **React Router** - Клиентская маршрутизация
- **Axios** - HTTP клиент
- **React Hook Form** - Управление формами

## ✨ Функциональность

### 👥 Роли пользователей
- **👨‍🎓 Студент** - Просмотр курсов, прохождение тестов, отслеживание прогресса
- **👨‍🏫 Преподаватель** - Создание курсов и тестов, управление контентом
- **👨‍💼 Администратор** - Полный доступ ко всем функциям системы

### 🎯 Основные возможности
- ✅ **Аутентификация** - Регистрация, вход, JWT токены
- ✅ **Управление курсами** - CRUD операции для курсов
- ✅ **Система тестирования** - Создание тестов с различными типами вопросов
- ✅ **Прохождение тестов** - Ограничения по времени и количеству попыток
- ✅ **Отслеживание результатов** - Детальная статистика по попыткам
- ✅ **Поиск и фильтрация** - Быстрый поиск по курсам
- ✅ **Адаптивный дизайн** - Работа на всех устройствах

## 🚀 Быстрый старт

### Предварительные требования
- ☕ **Java 17+**
- 🟢 **Node.js 16+**
- 🐘 **PostgreSQL 12+**
- 📦 **Maven 3.6+**

### 1. Клонирование и установка

```bash
# Клонируйте репозиторий
git clone <repository-url>
cd course-platform

# Установите зависимости фронтенда
cd frontend
npm install
cd ..
```

### 2. Настройка базы данных

```sql
CREATE DATABASE course_platform;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE course_platform TO postgres;
```

### 3. Запуск приложения

**Автоматический запуск:**

Windows:
```cmd
start-dev.bat
```

Linux/Mac:
```bash
chmod +x start-dev.sh
./start-dev.sh
```

**Ручной запуск:**

Терминал 1 - Бэкенд:
```bash
cd backend
mvn spring-boot:run
```

Терминал 2 - Фронтенд:
```bash
cd frontend
npm start
```

### 4. Доступ к приложению

- 🌐 **Веб-интерфейс**: http://localhost:3000
- 🔧 **API**: http://localhost:8080
- 📚 **Swagger UI**: http://localhost:8080/swagger-ui.html

## 📖 Документация

- 📁 [Документация бэкенда](backend/README.md)
- 📁 [Документация фронтенда](frontend/README.md)
- 🚀 [Быстрый старт](QUICK_START.md)

## 🧪 Тестирование

### Создание тестовых пользователей

1. Откройте http://localhost:3000
2. Нажмите "Зарегистрироваться"
3. Создайте пользователей с разными ролями:

**Администратор:**
- Email: admin@example.com
- Password: admin123
- Role: ADMIN

**Преподаватель:**
- Email: teacher@example.com
- Password: teacher123
- Role: TEACHER

**Студент:**
- Email: student@example.com
- Password: student123
- Role: STUDENT

### Тестовый сценарий

1. **Войдите как преподаватель** и создайте курс
2. **Добавьте тест** к курсу с различными типами вопросов
3. **Войдите как студент** и пройдите тест
4. **Проверьте результаты** в интерфейсе преподавателя

## 🔐 Безопасность

- **JWT аутентификация** с настраиваемым временем жизни
- **Хэширование паролей** с использованием BCrypt
- **Авторизация на основе ролей** для всех эндпоинтов
- **CORS настройки** для безопасного взаимодействия фронтенда с API
- **Валидация входных данных** на всех уровнях

## 🚀 Деплой

### Backend
```bash
cd backend
mvn clean package
java -jar -Dspring.profiles.active=prod target/course-platform-1.0.0.jar
```

### Frontend
```bash
cd frontend
npm run build
# Разверните папку build/ на веб-сервере (nginx, Apache)
```

## 🤝 Участие в разработке

### Структура веток
- `main` - Стабильная версия
- `develop` - Разработка
- `feature/*` - Новые функции
- `bugfix/*` - Исправления ошибок

### Процесс разработки
1. Создайте ветку от `develop`
2. Внесите изменения
3. Создайте Pull Request
4. Пройдите код-ревью
5. Слияние в `develop`

### Добавление новых функций

**Backend:**
1. Создайте сущность в `backend/src/main/java/edu/platform/entity/`
2. Добавьте репозиторий в `repository/`
3. Создайте сервис в `service/`
4. Добавьте контроллер в `controller/`
5. Создайте DTO в `dto/`

**Frontend:**
1. Добавьте типы в `frontend/src/types/`
2. Создайте API методы в `services/`
3. Добавьте компоненты в `components/`
4. Создайте страницы в `pages/`
5. Обновите маршруты в `App.tsx`

## 📊 Мониторинг

### Backend метрики
- Spring Boot Actuator: http://localhost:8080/actuator
- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics

### Логирование
- Уровень логирования настраивается в `application.yml`
- Логи приложения: `DEBUG` уровень для разработки
- Логи безопасности: отдельный логгер для аудита

## 🐛 Устранение неполадок

### Общие проблемы

**Ошибка подключения к БД:**
- Проверьте, что PostgreSQL запущен
- Убедитесь в правильности настроек в `application.yml`
- Проверьте доступность порта 5433

**Ошибки CORS:**
- Убедитесь, что бэкенд запущен на порту 8080
- Проверьте настройки CORS в `SecurityConfig.java`

**Ошибки npm:**
- Удалите `node_modules` и `package-lock.json`
- Выполните `npm install` заново

### Логи и отладка

**Backend логи:**
```bash
cd backend
mvn spring-boot:run -Dlogging.level.edu.platform=DEBUG
```

**Frontend отладка:**
- Откройте Developer Tools в браузере
- Проверьте вкладку Console на наличие ошибок
- Используйте Network tab для отладки API запросов

## 📝 Лицензия

Этот проект создан в учебных целях.

## 📞 Поддержка

При возникновении проблем:
1. Проверьте [документацию](QUICK_START.md)
2. Просмотрите [Issues](../../issues)
3. Создайте новый Issue с подробным описанием проблемы

---

**Приятного использования! 🎉**