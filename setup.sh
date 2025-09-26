#!/bin/bash

echo "🚀 Настройка платформы курсов..."

# Проверяем наличие Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js не найден. Пожалуйста, установите Node.js 16+ с https://nodejs.org/"
    exit 1
fi

# Проверяем наличие Java
if ! command -v java &> /dev/null; then
    echo "❌ Java не найден. Пожалуйста, установите Java 17+ с https://adoptium.net/"
    exit 1
fi

# Проверяем наличие Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven не найден. Пожалуйста, установите Maven с https://maven.apache.org/"
    exit 1
fi

echo "✅ Все необходимые инструменты найдены"

# Устанавливаем зависимости фронтенда
echo "📦 Установка зависимостей фронтенда..."
cd frontend
npm install

if [ $? -eq 0 ]; then
    echo "✅ Зависимости фронтенда установлены успешно"
else
    echo "❌ Ошибка при установке зависимостей фронтенда"
    exit 1
fi

cd ..

echo "🎉 Настройка завершена!"
echo ""
echo "📋 Следующие шаги:"
echo "1. Настройте PostgreSQL базу данных (см. README_FULL.md)"
echo "2. Запустите бэкенд: mvn spring-boot:run"
echo "3. В новом терминале запустите фронтенд: cd frontend && npm start"
echo ""
echo "🌐 После запуска:"
echo "   - Фронтенд: http://localhost:3000"
echo "   - Бэкенд API: http://localhost:8080"
echo "   - Swagger UI: http://localhost:8080/swagger-ui.html"
