@echo off
echo 🚀 Запуск платформы курсов в режиме разработки...
echo.

REM Проверяем, запущен ли бэкенд
echo 🔍 Проверяем бэкенд на порту 8080...
netstat -an | findstr :8080 >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Бэкенд не запущен. Запустите его командой: cd backend && mvn spring-boot:run
    echo.
) else (
    echo ✅ Бэкенд уже запущен на порту 8080
    echo.
)

REM Проверяем, запущен ли фронтенд
echo 🔍 Проверяем фронтенд на порту 3000...
netstat -an | findstr :3000 >nul 2>&1
if errorlevel 1 (
    echo 🌐 Запускаем фронтенд...
    cd frontend
    start "Frontend Server" cmd /k "npm start"
    cd ..
    echo ✅ Фронтенд запускается...
) else (
    echo ✅ Фронтенд уже запущен на порту 3000
)

echo.
echo 🎉 Готово! Приложение будет доступно по адресам:
echo    - Фронтенд: http://localhost:3000
echo    - Бэкенд API: http://localhost:8080
echo    - Swagger UI: http://localhost:8080/swagger-ui.html
echo.
echo 📝 Для остановки закройте окна терминалов или нажмите Ctrl+C
pause
