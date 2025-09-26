#!/bin/bash

echo "🚀 Запуск фронтенда Course Platform..."
echo

# Проверяем наличие Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js не найден. Пожалуйста, установите Node.js 16+ с https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js найден"
echo

echo "🔍 Проверяем порт 3000..."
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Порт 3000 уже занят. Остановите другие приложения на этом порту."
    exit 1
fi

echo "📦 Проверяем зависимости..."
if [ ! -d "node_modules" ]; then
    echo "📥 Устанавливаем зависимости..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ Ошибка при установке зависимостей"
        exit 1
    fi
fi

echo "🚀 Запуск React приложения..."
npm start
