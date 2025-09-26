# 🚀 Руководство по развертыванию

Инструкции по развертыванию Course Management Platform в различных окружениях.

## 📋 Содержание

- [Локальная разработка](#локальная-разработка)
- [Продакшн развертывание](#продакшн-развертывание)
- [Docker развертывание](#docker-развертывание)
- [Настройка базы данных](#настройка-базы-данных)
- [Мониторинг и логирование](#мониторинг-и-логирование)

## 🛠️ Локальная разработка

### Быстрый старт

1. **Клонирование репозитория:**
   ```bash
   git clone <repository-url>
   cd course-platform
   ```

2. **Автоматический запуск:**
   ```bash
   # Windows
   start-dev.bat
   
   # Linux/Mac
   chmod +x start-dev.sh
   ./start-dev.sh
   ```

3. **Ручной запуск:**
   ```bash
   # Терминал 1 - Backend
   cd backend
   mvn spring-boot:run
   
   # Терминал 2 - Frontend
   cd frontend
   npm install
   npm start
   ```

### Настройка окружения разработки

**Backend (Spring Boot):**
- Порт: 8080
- Профиль: `default`
- База данных: PostgreSQL (localhost:5433)
- Логи: DEBUG уровень

**Frontend (React):**
- Порт: 3000
- Режим: development
- Proxy: http://localhost:8080
- Hot reload: включен

## 🌐 Продакшн развертывание

### Backend (Spring Boot)

1. **Сборка приложения:**
   ```bash
   cd backend
   mvn clean package -Pprod
   ```

2. **Настройка продакшн конфигурации:**
   
   Создайте `application-prod.yml`:
   ```yaml
   server:
     port: 8080
   
   spring:
     datasource:
       url: jdbc:postgresql://your-db-host:5432/course_platform
       username: ${DB_USERNAME}
       password: ${DB_PASSWORD}
     
     jpa:
       hibernate:
         ddl-auto: validate
       show-sql: false
     
     security:
       jwt:
         secret: ${JWT_SECRET}
         expiration: 86400000
   
   logging:
     level:
       root: INFO
       edu.platform: INFO
     file:
       name: /var/log/course-platform/application.log
   ```

3. **Запуск в продакшене:**
   ```bash
   java -jar -Dspring.profiles.active=prod \
        -DDB_USERNAME=your_username \
        -DDB_PASSWORD=your_password \
        -DJWT_SECRET=your_jwt_secret \
        target/course-platform-1.0.0.jar
   ```

### Frontend (React)

1. **Сборка приложения:**
   ```bash
   cd frontend
   npm run build
   ```

2. **Настройка веб-сервера (nginx):**
   
   `/etc/nginx/sites-available/course-platform`:
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       root /path/to/course-platform/frontend/build;
       index index.html;
       
       # Обслуживание статических файлов
       location / {
           try_files $uri $uri/ /index.html;
       }
       
       # Проксирование API запросов
       location /api/ {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
       
       # Кэширование статических ресурсов
       location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
           expires 1y;
           add_header Cache-Control "public, immutable";
       }
   }
   ```

3. **Активация конфигурации:**
   ```bash
   sudo ln -s /etc/nginx/sites-available/course-platform /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl reload nginx
   ```

## 🐳 Docker развертывание

### Dockerfile для Backend

`backend/Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "-Dspring.profiles.active=prod", "target/course-platform-1.0.0.jar"]
```

### Dockerfile для Frontend

`frontend/Dockerfile`:
```dockerfile
FROM node:16-alpine as build

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

`docker-compose.yml`:
```yaml
version: '3.8'

services:
  database:
    image: postgres:13
    environment:
      POSTGRES_DB: course_platform
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  backend:
    build: ./backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_USERNAME=postgres
      - DB_PASSWORD=password
      - JWT_SECRET=your_jwt_secret_here
    depends_on:
      - database
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    depends_on:
      - backend
    ports:
      - "80:80"

volumes:
  postgres_data:
```

### Запуск с Docker Compose

```bash
docker-compose up -d
```

## 🗄️ Настройка базы данных

### PostgreSQL в продакшене

1. **Установка PostgreSQL:**
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install postgresql postgresql-contrib
   
   # CentOS/RHEL
   sudo yum install postgresql-server postgresql-contrib
   sudo postgresql-setup initdb
   ```

2. **Создание базы данных:**
   ```sql
   sudo -u postgres psql
   
   CREATE DATABASE course_platform;
   CREATE USER course_user WITH PASSWORD 'secure_password';
   GRANT ALL PRIVILEGES ON DATABASE course_platform TO course_user;
   \q
   ```

3. **Настройка подключений:**
   
   Отредактируйте `/etc/postgresql/13/main/pg_hba.conf`:
   ```
   # Добавьте строку для приложения
   host    course_platform    course_user    127.0.0.1/32    md5
   ```

4. **Перезапуск PostgreSQL:**
   ```bash
   sudo systemctl restart postgresql
   ```

### Резервное копирование

1. **Создание бэкапа:**
   ```bash
   pg_dump -h localhost -U course_user -d course_platform > backup.sql
   ```

2. **Восстановление из бэкапа:**
   ```bash
   psql -h localhost -U course_user -d course_platform < backup.sql
   ```

3. **Автоматическое резервное копирование:**
   
   Создайте cron задачу:
   ```bash
   # Добавьте в crontab (crontab -e)
   0 2 * * * pg_dump -h localhost -U course_user -d course_platform > /backups/course_platform_$(date +\%Y\%m\%d).sql
   ```

## 📊 Мониторинг и логирование

### Логирование Backend

1. **Настройка логов в application-prod.yml:**
   ```yaml
   logging:
     level:
       root: INFO
       edu.platform: INFO
       org.springframework.security: WARN
     file:
       name: /var/log/course-platform/application.log
     pattern:
       file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
   ```

2. **Ротация логов с logrotate:**
   
   `/etc/logrotate.d/course-platform`:
   ```
   /var/log/course-platform/*.log {
       daily
       rotate 30
       compress
       delaycompress
       missingok
       create 644 course-platform course-platform
   }
   ```

### Мониторинг с помощью Actuator

1. **Включение Actuator endpoints:**
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,metrics,prometheus
     endpoint:
       health:
         show-details: always
   ```

2. **Доступные endpoints:**
   - `/actuator/health` - Состояние приложения
   - `/actuator/info` - Информация о приложении
   - `/actuator/metrics` - Метрики производительности
   - `/actuator/prometheus` - Метрики для Prometheus

### Системный мониторинг

1. **Мониторинг процессов:**
   ```bash
   # Проверка статуса приложения
   ps aux | grep java
   
   # Мониторинг ресурсов
   htop
   
   # Проверка портов
   netstat -tlnp | grep :8080
   ```

2. **Мониторинг логов:**
   ```bash
   # Просмотр логов в реальном времени
   tail -f /var/log/course-platform/application.log
   
   # Поиск ошибок
   grep ERROR /var/log/course-platform/application.log
   ```

## 🔒 Безопасность в продакшене

### SSL/TLS настройка

1. **Получение SSL сертификата (Let's Encrypt):**
   ```bash
   sudo apt install certbot python3-certbot-nginx
   sudo certbot --nginx -d your-domain.com
   ```

2. **Обновление nginx конфигурации:**
   ```nginx
   server {
       listen 443 ssl http2;
       server_name your-domain.com;
       
       ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
       
       # Остальная конфигурация...
   }
   ```

### Firewall настройка

```bash
# Разрешить только необходимые порты
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw enable
```

### Переменные окружения

Создайте `.env` файл для продакшена:
```bash
DB_USERNAME=course_user
DB_PASSWORD=secure_database_password
JWT_SECRET=very_long_and_secure_jwt_secret_key
SPRING_PROFILES_ACTIVE=prod
```

## 🚨 Устранение неполадок

### Общие проблемы

1. **Приложение не запускается:**
   ```bash
   # Проверьте логи
   tail -f /var/log/course-platform/application.log
   
   # Проверьте доступность БД
   pg_isready -h localhost -p 5432
   ```

2. **Ошибки подключения к БД:**
   ```bash
   # Проверьте статус PostgreSQL
   sudo systemctl status postgresql
   
   # Проверьте подключение
   psql -h localhost -U course_user -d course_platform
   ```

3. **Проблемы с производительностью:**
   ```bash
   # Мониторинг ресурсов
   top
   iostat
   
   # Анализ медленных запросов
   tail -f /var/log/postgresql/postgresql-13-main.log
   ```

### Контакты поддержки

При возникновении проблем:
1. Проверьте логи приложения
2. Изучите документацию
3. Создайте issue в репозитории с подробным описанием проблемы
