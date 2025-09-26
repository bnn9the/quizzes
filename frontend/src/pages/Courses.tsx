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
  TextField,
  InputAdornment,
  Fab,
} from '@mui/material';
import { Search, Add } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { courseAPI } from '../services/api';
import { Course, UserRole } from '../types';

const Courses: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [courses, setCourses] = useState<Course[]>([]);
  const [filteredCourses, setFilteredCourses] = useState<Course[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    fetchCourses();
  }, []);

  useEffect(() => {
    // Фильтрация курсов по поисковому запросу
    if (searchQuery.trim()) {
      const filtered = courses.filter(course =>
        course.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        course.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        `${course.teacher.firstName} ${course.teacher.lastName}`.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setFilteredCourses(filtered);
    } else {
      setFilteredCourses(courses);
    }
  }, [courses, searchQuery]);

  const fetchCourses = async () => {
    try {
      setIsLoading(true);
      const response = await courseAPI.getAllCourses();
      setCourses(response.data);
    } catch (err: any) {
      setError('Ошибка при загрузке курсов');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(event.target.value);
  };

  const canCreateCourse = user?.role === UserRole.TEACHER || user?.role === UserRole.ADMIN;

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
          Курсы
        </Typography>
        {canCreateCourse && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => navigate('/courses/create')}
          >
            Создать курс
          </Button>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Поиск */}
      <Box sx={{ mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Поиск курсов..."
          value={searchQuery}
          onChange={handleSearchChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search />
              </InputAdornment>
            ),
          }}
        />
      </Box>

      {/* Список курсов */}
      {filteredCourses.length === 0 ? (
        <Box textAlign="center" py={4}>
          <Typography variant="h6" color="text.secondary">
            {searchQuery ? 'Курсы не найдены' : 'Курсы пока не созданы'}
          </Typography>
          {canCreateCourse && !searchQuery && (
            <Button
              variant="contained"
              sx={{ mt: 2 }}
              onClick={() => navigate('/courses/create')}
            >
              Создать первый курс
            </Button>
          )}
        </Box>
      ) : (
        <Grid container spacing={3}>
          {filteredCourses.map((course) => (
            <Grid item xs={12} sm={6} md={4} key={course.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" gutterBottom>
                    {course.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {course.description}
                  </Typography>
                  <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Chip 
                      label={`${course.teacher.firstName} ${course.teacher.lastName}`}
                      size="small"
                      variant="outlined"
                    />
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
                    onClick={() => navigate(`/courses/${course.id}`)}
                    variant="contained"
                  >
                    Подробнее
                  </Button>
                  {(user?.role === UserRole.TEACHER && course.teacher.id === user.id) || 
                   user?.role === UserRole.ADMIN ? (
                    <Button 
                      size="small" 
                      onClick={() => navigate(`/courses/${course.id}/edit`)}
                    >
                      Редактировать
                    </Button>
                  ) : null}
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Плавающая кнопка для создания курса на мобильных */}
      {canCreateCourse && (
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
      )}
    </Container>
  );
};

export default Courses;
