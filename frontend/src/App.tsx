import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import Courses from './pages/Courses';
import CourseDetail from './pages/CourseDetail';
import CourseForm from './pages/CourseForm';
import MyCourses from './pages/MyCourses';
import QuizDetail from './pages/QuizDetail';
import { UserRole } from './types';

// Создаем тему Material-UI
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
  typography: {
    fontFamily: 'Roboto, Arial, sans-serif',
  },
});

// Компонент для страницы "Не авторизован"
const UnauthorizedPage: React.FC = () => (
  <div style={{ textAlign: 'center', marginTop: '50px' }}>
    <h1>Доступ запрещен</h1>
    <p>У вас нет прав для просмотра этой страницы.</p>
  </div>
);

// Компонент для страницы "Не найдено"
const NotFoundPage: React.FC = () => (
  <div style={{ textAlign: 'center', marginTop: '50px' }}>
    <h1>Страница не найдена</h1>
    <p>Запрашиваемая страница не существует.</p>
  </div>
);

const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SnackbarProvider maxSnack={3}>
        <AuthProvider>
          <Router>
            <Routes>
              {/* Публичные маршруты */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/unauthorized" element={<UnauthorizedPage />} />
              
              {/* Защищенные маршруты */}
              <Route
                path="/*"
                element={
                  <ProtectedRoute>
                    <Layout>
                      <Routes>
                        {/* Главная страница */}
                        <Route path="/" element={<Home />} />
                        
                        {/* Курсы */}
                        <Route path="/courses" element={<Courses />} />
                        <Route path="/courses/:id" element={<CourseDetail />} />
                        
                        {/* Мои курсы (только для преподавателей и админов) */}
                        <Route
                          path="/my-courses"
                          element={
                            <ProtectedRoute requiredRoles={[UserRole.TEACHER, UserRole.ADMIN]}>
                              <MyCourses />
                            </ProtectedRoute>
                          }
                        />
                        
                        {/* Создание и редактирование курсов (только для преподавателей и админов) */}
                        <Route
                          path="/courses/create"
                          element={
                            <ProtectedRoute requiredRoles={[UserRole.TEACHER, UserRole.ADMIN]}>
                              <CourseForm />
                            </ProtectedRoute>
                          }
                        />
                        <Route
                          path="/courses/:id/edit"
                          element={
                            <ProtectedRoute requiredRoles={[UserRole.TEACHER, UserRole.ADMIN]}>
                              <CourseForm />
                            </ProtectedRoute>
                          }
                        />
                        
                        {/* Тесты */}
                        <Route path="/quizzes/:id" element={<QuizDetail />} />
                        
                        {/* Страница не найдена */}
                        <Route path="*" element={<NotFoundPage />} />
                      </Routes>
                    </Layout>
                  </ProtectedRoute>
                }
              />
            </Routes>
          </Router>
        </AuthProvider>
      </SnackbarProvider>
    </ThemeProvider>
  );
};

export default App;
