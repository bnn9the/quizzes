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
  Fab,
} from '@mui/material';
import { Add, Edit, Visibility } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { courseAPI } from '../services/api';
import { Course } from '../types';

const MyCourses: React.FC = () => {
  const navigate = useNavigate();
  const [courses, setCourses] = useState<Course[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    fetchMyCourses();
  }, []);

  const fetchMyCourses = async () => {
    try {
      setIsLoading(true);
      const response = await courseAPI.getMyCourses();
      setCourses(response.data);
    } catch (err: any) {
      setError('Ошибка при загрузке ваших курсов');
    } finally {
      setIsLoading(false);
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
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" component="h1">
          Мои курсы
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => navigate('/courses/create')}
        >
          Создать курс
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {courses.length === 0 ? (
        <Box textAlign="center" py={4}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            У вас пока нет созданных курсов
          </Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/courses/create')}
          >
            Создать первый курс
          </Button>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {courses.map((course) => (
            <Grid item xs={12} sm={6} md={4} key={course.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" gutterBottom>
                    {course.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {course.description}
                  </Typography>
                  <Box sx={{ mt: 2 }}>
                    <Chip 
                      label={new Date(course.createdAt).toLocaleDateString('ru-RU')}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                  </Box>
                </CardContent>
                <CardActions>
                  <Button 
                    size="small" 
                    startIcon={<Visibility />}
                    onClick={() => navigate(`/courses/${course.id}`)}
                  >
                    Просмотр
                  </Button>
                  <Button 
                    size="small" 
                    startIcon={<Edit />}
                    onClick={() => navigate(`/courses/${course.id}/edit`)}
                  >
                    Редактировать
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Fab
        color="primary"
        aria-label="add"
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
          display: { xs: 'flex', sm: 'none' }
        }}
        onClick={() => navigate('/courses/create')}
      >
        <Add />
      </Fab>
    </Container>
  );
};

export default MyCourses;
