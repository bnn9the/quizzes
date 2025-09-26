import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Box,
  Chip,
  CircularProgress,
  Alert,
} from '@mui/material';
import { School, Quiz, TrendingUp } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { courseAPI, quizAPI } from '../services/api';
import { Course, Quiz as QuizType, UserRole } from '../types';

const Home: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [courses, setCourses] = useState<Course[]>([]);
  const [quizzes, setQuizzes] = useState<QuizType[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        
        // Загружаем курсы
        const coursesResponse = await courseAPI.getAllCourses();
        setCourses(coursesResponse.data.slice(0, 6)); // Показываем только первые 6

        // Для студентов показываем доступные квизы
        if (user?.role === UserRole.STUDENT && coursesResponse.data.length > 0) {
          const allQuizzes: QuizType[] = [];
          for (const course of coursesResponse.data.slice(0, 3)) {
            try {
              const quizzesResponse = await quizAPI.getQuizzesByCourse(course.id);
              allQuizzes.push(...quizzesResponse.data.filter(q => q.isActive));
            } catch (err) {
              // Игнорируем ошибки для отдельных курсов
            }
          }
          setQuizzes(allQuizzes.slice(0, 6));
        }
      } catch (err: any) {
        setError('Ошибка при загрузке данных');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [user]);

  const getWelcomeMessage = () => {
    const firstName = user?.firstName || 'Пользователь';
    switch (user?.role) {
      case UserRole.STUDENT:
        return `Добро пожаловать, ${firstName}! Изучайте курсы и проходите тесты.`;
      case UserRole.TEACHER:
        return `Добро пожаловать, ${firstName}! Создавайте курсы и тесты для студентов.`;
      case UserRole.ADMIN:
        return `Добро пожаловать, ${firstName}! Управляйте платформой.`;
      default:
        return `Добро пожаловать, ${firstName}!`;
    }
  };

  const getQuickActions = () => {
    switch (user?.role) {
      case UserRole.STUDENT:
        return [
          { text: 'Просмотреть курсы', path: '/courses', icon: <School /> },
          { text: 'Мои результаты', path: '/my-attempts', icon: <TrendingUp /> },
        ];
      case UserRole.TEACHER:
        return [
          { text: 'Мои курсы', path: '/my-courses', icon: <School /> },
          { text: 'Создать курс', path: '/courses/create', icon: <School /> },
          { text: 'Мои тесты', path: '/my-quizzes', icon: <Quiz /> },
        ];
      case UserRole.ADMIN:
        return [
          { text: 'Управление курсами', path: '/admin/courses', icon: <School /> },
          { text: 'Управление тестами', path: '/admin/quizzes', icon: <Quiz /> },
        ];
      default:
        return [];
    }
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Платформа курсов
        </Typography>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          {getWelcomeMessage()}
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Быстрые действия */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" gutterBottom>
          Быстрые действия
        </Typography>
        <Grid container spacing={2}>
          {getQuickActions().map((action, index) => (
            <Grid item xs={12} sm={6} md={4} key={index}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    {action.icon}
                    <Typography variant="h6">{action.text}</Typography>
                  </Box>
                </CardContent>
                <CardActions>
                  <Button 
                    size="small" 
                    onClick={() => navigate(action.path)}
                    variant="contained"
                  >
                    Перейти
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>

      {/* Последние курсы */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" gutterBottom>
          Доступные курсы
        </Typography>
        <Grid container spacing={3}>
          {courses.map((course) => (
            <Grid item xs={12} sm={6} md={4} key={course.id}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {course.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {course.description}
                  </Typography>
                  <Box sx={{ mt: 1 }}>
                    <Chip 
                      label={`Преподаватель: ${course.teacher.firstName} ${course.teacher.lastName}`}
                      size="small"
                      variant="outlined"
                    />
                  </Box>
                </CardContent>
                <CardActions>
                  <Button 
                    size="small" 
                    onClick={() => navigate(`/courses/${course.id}`)}
                  >
                    Подробнее
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
        {courses.length > 0 && (
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Button 
              variant="outlined" 
              onClick={() => navigate('/courses')}
            >
              Посмотреть все курсы
            </Button>
          </Box>
        )}
      </Box>

      {/* Доступные тесты для студентов */}
      {user?.role === UserRole.STUDENT && quizzes.length > 0 && (
        <Box sx={{ mb: 4 }}>
          <Typography variant="h5" gutterBottom>
            Доступные тесты
          </Typography>
          <Grid container spacing={3}>
            {quizzes.map((quiz) => (
              <Grid item xs={12} sm={6} md={4} key={quiz.id}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      {quiz.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      {quiz.description}
                    </Typography>
                    <Box sx={{ mt: 1, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      <Chip 
                        label={quiz.course.title}
                        size="small"
                        color="primary"
                      />
                      {quiz.timeLimitMinutes && (
                        <Chip 
                          label={`${quiz.timeLimitMinutes} мин`}
                          size="small"
                          variant="outlined"
                        />
                      )}
                    </Box>
                  </CardContent>
                  <CardActions>
                    <Button 
                      size="small" 
                      onClick={() => navigate(`/quizzes/${quiz.id}`)}
                      variant="contained"
                    >
                      Пройти тест
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      )}
    </Container>
  );
};

export default Home;
