@echo off
echo 🚀 Запуск бэкенда Course Platform...
echo.

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

echo ✅ Java и Maven найдены
echo.

echo 🔍 Проверяем порт 8080...
netstat -an | findstr :8080 >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  Порт 8080 уже занят. Остановите другие приложения на этом порту.
    pause
    exit /b 1
)

echo 🚀 Запуск Spring Boot приложения...
mvn spring-boot:run

pause
