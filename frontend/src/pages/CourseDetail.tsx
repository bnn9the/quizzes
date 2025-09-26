import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  CircularProgress,
  Alert,
  Grid,
  Divider,
} from '@mui/material';
import { Edit, Delete, Quiz as QuizIcon, Person } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { courseAPI, quizAPI } from '../services/api';
import { Course, Quiz, UserRole } from '../types';

const CourseDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [course, setCourse] = useState<Course | null>(null);
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    if (id) {
      fetchCourseData();
    }
  }, [id]);

  const fetchCourseData = async () => {
    try {
      setIsLoading(true);
      const courseId = parseInt(id!);
      
      // Загружаем курс
      const courseResponse = await courseAPI.getCourseById(courseId);
      setCourse(courseResponse.data);
      
      // Загружаем квизы для курса
      const quizzesResponse = await quizAPI.getQuizzesByCourse(courseId);
      setQuizzes(quizzesResponse.data);
    } catch (err: any) {
      setError('Ошибка при загрузке курса');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteCourse = async () => {
    if (!course || !window.confirm('Вы уверены, что хотите удалить этот курс?')) {
      return;
    }

    try {
      await courseAPI.deleteCourse(course.id);
      navigate('/courses');
    } catch (err: any) {
      setError('Ошибка при удалении курса');
    }
  };

  const canEditCourse = () => {
    if (!user || !course) return false;
    return user.role === UserRole.ADMIN || 
           (user.role === UserRole.TEACHER && course.teacher.id === user.id);
  };

  const canCreateQuiz = () => {
    return canEditCourse();
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!course) {
    return (
      <Container maxWidth="lg">
        <Alert severity="error">
          Курс не найден
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Информация о курсе */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
            <Typography variant="h4" component="h1">
              {course.title}
            </Typography>
            {canEditCourse() && (
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  variant="outlined"
                  startIcon={<Edit />}
                  onClick={() => navigate(`/courses/${course.id}/edit`)}
                >
                  Редактировать
                </Button>
                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<Delete />}
                  onClick={handleDeleteCourse}
                >
                  Удалить
                </Button>
              </Box>
            )}
          </Box>
          
          <Typography variant="body1" paragraph>
            {course.description}
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
            <Chip
              icon={<Person />}
              label={`Преподаватель: ${course.teacher.firstName} ${course.teacher.lastName}`}
              variant="outlined"
            />
            <Chip
              label={`Создан: ${new Date(course.createdAt).toLocaleDateString('ru-RU')}`}
              variant="outlined"
            />
            {course.updatedAt !== course.createdAt && (
              <Chip
                label={`Обновлен: ${new Date(course.updatedAt).toLocaleDateString('ru-RU')}`}
                variant="outlined"
              />
            )}
          </Box>
        </CardContent>
      </Card>

      <Divider sx={{ mb: 3 }} />

      {/* Тесты курса */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h2">
            Тесты курса
          </Typography>
          {canCreateQuiz() && (
            <Button
              variant="contained"
              startIcon={<QuizIcon />}
              onClick={() => navigate(`/courses/${course.id}/quizzes/create`)}
            >
              Создать тест
            </Button>
          )}
        </Box>

        {quizzes.length === 0 ? (
          <Box textAlign="center" py={4}>
            <Typography variant="h6" color="text.secondary">
              Тесты для этого курса пока не созданы
            </Typography>
            {canCreateQuiz() && (
              <Button
                variant="contained"
                sx={{ mt: 2 }}
                onClick={() => navigate(`/courses/${course.id}/quizzes/create`)}
              >
                Создать первый тест
              </Button>
            )}
          </Box>
        ) : (
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
                    <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      <Chip
                        label={quiz.isActive ? 'Активен' : 'Неактивен'}
                        size="small"
                        color={quiz.isActive ? 'success' : 'default'}
                      />
                      {quiz.maxAttempts && (
                        <Chip
                          label={`Попыток: ${quiz.maxAttempts}`}
                          size="small"
                          variant="outlined"
                        />
                      )}
                      {quiz.timeLimitMinutes && (
                        <Chip
                          label={`${quiz.timeLimitMinutes} мин`}
                          size="small"
                          variant="outlined"
                        />
                      )}
                      {quiz.questions && (
                        <Chip
                          label={`Вопросов: ${quiz.questions.length}`}
                          size="small"
                          variant="outlined"
                        />
                      )}
                    </Box>
                  </CardContent>
                  <CardActions>
                    {user?.role === UserRole.STUDENT && quiz.isActive ? (
                      <Button
                        size="small"
                        variant="contained"
                        onClick={() => navigate(`/quizzes/${quiz.id}`)}
                      >
                        Пройти тест
                      </Button>
                    ) : (
                      <Button
                        size="small"
                        onClick={() => navigate(`/quizzes/${quiz.id}`)}
                      >
                        Подробнее
                      </Button>
                    )}
                    {canEditCourse() && (
                      <Button
                        size="small"
                        onClick={() => navigate(`/quizzes/${quiz.id}/edit`)}
                      >
                        Редактировать
                      </Button>
                    )}
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Box>
    </Container>
  );
};

export default CourseDetail;
