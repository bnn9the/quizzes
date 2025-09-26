import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate, useParams } from 'react-router-dom';
import { courseAPI } from '../services/api';
import { CourseRequest, Course } from '../types';

const schema = yup.object({
  title: yup
    .string()
    .required('Название курса обязательно')
    .min(3, 'Название должно содержать минимум 3 символа'),
  description: yup
    .string()
    .required('Описание курса обязательно')
    .min(10, 'Описание должно содержать минимум 10 символов'),
});

const CourseForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingCourse, setIsLoadingCourse] = useState(!!id);
  
  const isEditing = !!id;

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<CourseRequest>({
    resolver: yupResolver(schema),
  });

  useEffect(() => {
    if (isEditing) {
      fetchCourse();
    }
  }, [id, isEditing]);

  const fetchCourse = async () => {
    try {
      setIsLoadingCourse(true);
      const response = await courseAPI.getCourseById(parseInt(id!));
      const course: Course = response.data;
      
      setValue('title', course.title);
      setValue('description', course.description);
    } catch (err: any) {
      setError('Ошибка при загрузке курса');
    } finally {
      setIsLoadingCourse(false);
    }
  };

  const onSubmit = async (data: CourseRequest) => {
    try {
      setIsLoading(true);
      setError('');
      
      if (isEditing) {
        await courseAPI.updateCourse(parseInt(id!), data);
      } else {
        await courseAPI.createCourse(data);
      }
      
      navigate('/courses');
    } catch (err: any) {
      setError(
        err.response?.data?.message || 
        `Ошибка при ${isEditing ? 'обновлении' : 'создании'} курса`
      );
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoadingCourse) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container component="main" maxWidth="md">
      <Box
        sx={{
          marginTop: 4,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ padding: 4, width: '100%' }}>
          <Typography component="h1" variant="h4" align="center" gutterBottom>
            {isEditing ? 'Редактировать курс' : 'Создать новый курс'}
          </Typography>
          
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="title"
              label="Название курса"
              autoFocus
              {...register('title')}
              error={!!errors.title}
              helperText={errors.title?.message}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="description"
              label="Описание курса"
              multiline
              rows={4}
              {...register('description')}
              error={!!errors.description}
              helperText={errors.description?.message}
            />
            
            <Box sx={{ mt: 3, display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
              <Button
                variant="outlined"
                onClick={() => navigate('/courses')}
                disabled={isLoading}
              >
                Отмена
              </Button>
              <Button
                type="submit"
                variant="contained"
                disabled={isLoading}
              >
                {isLoading ? (
                  <CircularProgress size={24} />
                ) : (
                  isEditing ? 'Обновить курс' : 'Создать курс'
                )}
              </Button>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default CourseForm;
