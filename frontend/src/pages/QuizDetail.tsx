import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Button,
  Chip,
  CircularProgress,
  Alert,
  Divider,
  List,
  ListItem,
  ListItemText,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import { PlayArrow, Edit, Delete, Timer, Assignment } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { quizAPI } from '../services/api';
import { Quiz, UserRole, QuestionType } from '../types';

const QuizDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [confirmStart, setConfirmStart] = useState(false);

  useEffect(() => {
    if (id) {
      fetchQuiz();
    }
  }, [id]);

  const fetchQuiz = async () => {
    try {
      setIsLoading(true);
      const response = await quizAPI.getQuizById(parseInt(id!));
      setQuiz(response.data);
    } catch (err: any) {
      setError('Ошибка при загрузке теста');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStartQuiz = async () => {
    if (!quiz) return;

    try {
      const response = await quizAPI.startQuizAttempt(quiz.id);
      navigate(`/quiz-attempt/${response.data.id}`);
    } catch (err: any) {
      setError('Ошибка при начале прохождения теста');
    }
  };

  const handleDeleteQuiz = async () => {
    if (!quiz || !window.confirm('Вы уверены, что хотите удалить этот тест?')) {
      return;
    }

    try {
      await quizAPI.deleteQuiz(quiz.id);
      navigate(`/courses/${quiz.course.id}`);
    } catch (err: any) {
      setError('Ошибка при удалении теста');
    }
  };

  const canEditQuiz = () => {
    if (!user || !quiz) return false;
    return user.role === UserRole.ADMIN || 
           (user.role === UserRole.TEACHER && quiz.course.teacher.id === user.id);
  };

  const canTakeQuiz = () => {
    if (!user || !quiz) return false;
    return user.role === UserRole.STUDENT && quiz.isActive;
  };

  const getQuestionTypeLabel = (type: QuestionType) => {
    switch (type) {
      case QuestionType.SINGLE_CHOICE:
        return 'Одиночный выбор';
      case QuestionType.MULTIPLE_CHOICE:
        return 'Множественный выбор';
      case QuestionType.TRUE_FALSE:
        return 'Верно/Неверно';
      case QuestionType.TEXT:
        return 'Текстовый ответ';
      default:
        return type;
    }
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!quiz) {
    return (
      <Container maxWidth="lg">
        <Alert severity="error">
          Тест не найден
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

      {/* Информация о тесте */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
            <Typography variant="h4" component="h1">
              {quiz.title}
            </Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              {canTakeQuiz() && (
                <Button
                  variant="contained"
                  startIcon={<PlayArrow />}
                  onClick={() => setConfirmStart(true)}
                  size="large"
                >
                  Пройти тест
                </Button>
              )}
              {canEditQuiz() && (
                <>
                  <Button
                    variant="outlined"
                    startIcon={<Edit />}
                    onClick={() => navigate(`/quizzes/${quiz.id}/edit`)}
                  >
                    Редактировать
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<Delete />}
                    onClick={handleDeleteQuiz}
                  >
                    Удалить
                  </Button>
                </>
              )}
            </Box>
          </Box>
          
          <Typography variant="body1" paragraph>
            {quiz.description}
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center', mb: 2 }}>
            <Chip
              label={quiz.course.title}
              color="primary"
              variant="outlined"
            />
            <Chip
              label={quiz.isActive ? 'Активен' : 'Неактивен'}
              color={quiz.isActive ? 'success' : 'default'}
            />
            {quiz.maxAttempts && (
              <Chip
                icon={<Assignment />}
                label={`Максимум попыток: ${quiz.maxAttempts}`}
                variant="outlined"
              />
            )}
            {quiz.timeLimitMinutes && (
              <Chip
                icon={<Timer />}
                label={`Время: ${quiz.timeLimitMinutes} мин`}
                variant="outlined"
              />
            )}
          </Box>
        </CardContent>
      </Card>

      <Divider sx={{ mb: 3 }} />

      {/* Вопросы теста */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" component="h2" gutterBottom>
          Вопросы теста ({quiz.questions?.length || 0})
        </Typography>

        {quiz.questions && quiz.questions.length > 0 ? (
          <List>
            {quiz.questions.map((question, index) => (
              <Card key={question.id} sx={{ mb: 2 }}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Typography variant="h6">
                      Вопрос {index + 1}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Chip
                        label={getQuestionTypeLabel(question.type)}
                        size="small"
                        variant="outlined"
                      />
                      <Chip
                        label={`${question.points} баллов`}
                        size="small"
                        color="primary"
                      />
                    </Box>
                  </Box>
                  
                  <Typography variant="body1" gutterBottom>
                    {question.text}
                  </Typography>
                  
                  {question.answerOptions && question.answerOptions.length > 0 && (
                    <Box sx={{ mt: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>
                        Варианты ответов:
                      </Typography>
                      <List dense>
                        {question.answerOptions.map((option, optionIndex) => (
                          <ListItem key={option.id} sx={{ py: 0.5 }}>
                            <ListItemText
                              primary={`${String.fromCharCode(65 + optionIndex)}. ${option.text}`}
                              secondary={canEditQuiz() && option.isCorrect ? 'Правильный ответ' : undefined}
                            />
                          </ListItem>
                        ))}
                      </List>
                    </Box>
                  )}
                </CardContent>
              </Card>
            ))}
          </List>
        ) : (
          <Box textAlign="center" py={4}>
            <Typography variant="h6" color="text.secondary">
              Вопросы для этого теста пока не добавлены
            </Typography>
            {canEditQuiz() && (
              <Button
                variant="contained"
                sx={{ mt: 2 }}
                onClick={() => navigate(`/quizzes/${quiz.id}/edit`)}
              >
                Добавить вопросы
              </Button>
            )}
          </Box>
        )}
      </Box>

      {/* Диалог подтверждения начала теста */}
      <Dialog open={confirmStart} onClose={() => setConfirmStart(false)}>
        <DialogTitle>Начать прохождение теста?</DialogTitle>
        <DialogContent>
          <Typography>
            Вы собираетесь начать прохождение теста "{quiz.title}".
          </Typography>
          {quiz.timeLimitMinutes && (
            <Typography sx={{ mt: 1 }}>
              <strong>Внимание:</strong> У вас будет {quiz.timeLimitMinutes} минут на прохождение теста.
            </Typography>
          )}
          {quiz.maxAttempts && (
            <Typography sx={{ mt: 1 }}>
              <strong>Максимальное количество попыток:</strong> {quiz.maxAttempts}
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmStart(false)}>
            Отмена
          </Button>
          <Button onClick={handleStartQuiz} variant="contained">
            Начать тест
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default QuizDetail;
