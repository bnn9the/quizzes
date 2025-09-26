#!/bin/bash

echo "🚀 Запуск платформы курсов в режиме разработки..."
echo

# Проверяем, запущен ли бэкенд
echo "🔍 Проверяем бэкенд на порту 8080..."
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "✅ Бэкенд уже запущен на порту 8080"
else
    echo "⚠️  Бэкенд не запущен. Запустите его командой: cd backend && mvn spring-boot:run"
fi

echo

# Проверяем, запущен ли фронтенд
echo "🔍 Проверяем фронтенд на порту 3000..."
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
    echo "✅ Фронтенд уже запущен на порту 3000"
else
    echo "🌐 Запускаем фронтенд..."
    cd frontend
    npm start &
    cd ..
    echo "✅ Фронтенд запускается..."
fi

echo
echo "🎉 Готово! Приложение будет доступно по адресам:"
echo "   - Фронтенд: http://localhost:3000"
echo "   - Бэкенд API: http://localhost:8080"
echo "   - Swagger UI: http://localhost:8080/swagger-ui.html"
echo
echo "📝 Для остановки нажмите Ctrl+C"
