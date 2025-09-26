#!/bin/bash

echo "🚀 Запуск бэкенда Course Platform..."
echo

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

echo "✅ Java и Maven найдены"
echo

echo "🔍 Проверяем порт 8080..."
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Порт 8080 уже занят. Остановите другие приложения на этом порту."
    exit 1
fi

echo "🚀 Запуск Spring Boot приложения..."
mvn spring-boot:run
