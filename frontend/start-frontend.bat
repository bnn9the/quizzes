@echo off
echo 🚀 Запуск фронтенда Course Platform...
echo.

REM Проверяем наличие Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js не найден. Пожалуйста, установите Node.js 16+ с https://nodejs.org/
    pause
    exit /b 1
)

echo ✅ Node.js найден
echo.

echo 🔍 Проверяем порт 3000...
netstat -an | findstr :3000 >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  Порт 3000 уже занят. Остановите другие приложения на этом порту.
    pause
    exit /b 1
)

echo 📦 Проверяем зависимости...
if not exist "node_modules" (
    echo 📥 Устанавливаем зависимости...
    npm install
    if errorlevel 1 (
        echo ❌ Ошибка при установке зависимостей
        pause
        exit /b 1
    )
)

echo 🚀 Запуск React приложения...
npm start

pause
