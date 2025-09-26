@echo off
echo 🚀 Настройка платформы курсов...

REM Проверяем наличие Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js не найден. Пожалуйста, установите Node.js 16+ с https://nodejs.org/
    pause
    exit /b 1
)

REM Проверяем наличие Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java не найден. Пожалуйста, установите Java 17+ с https://adoptium.net/
    pause
    exit /b 1
)

REM Проверяем наличие Maven
mvn --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven не найден. Пожалуйста, установите Maven с https://maven.apache.org/
    pause
    exit /b 1
)

echo ✅ Все необходимые инструменты найдены

REM Устанавливаем зависимости фронтенда
echo 📦 Установка зависимостей фронтенда...
cd frontend
call npm install

if errorlevel 1 (
    echo ❌ Ошибка при установке зависимостей фронтенда
    pause
    exit /b 1
)

echo ✅ Зависимости фронтенда установлены успешно
cd ..

echo 🎉 Настройка завершена!
echo.
echo 📋 Следующие шаги:
echo 1. Настройте PostgreSQL базу данных (см. README_FULL.md)
echo 2. Запустите бэкенд: mvn spring-boot:run
echo 3. В новом терминале запустите фронтенд: cd frontend ^&^& npm start
echo.
echo 🌐 После запуска:
echo    - Фронтенд: http://localhost:3000
echo    - Бэкенд API: http://localhost:8080
echo    - Swagger UI: http://localhost:8080/swagger-ui.html

pause
