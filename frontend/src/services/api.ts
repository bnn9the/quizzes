import axios, { AxiosResponse } from 'axios';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  Course,
  CourseRequest,
  Quiz,
  QuizRequest,
  QuizAttempt,
  QuizSubmissionRequest,
} from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';

// Создаем экземпляр axios
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Интерсептор для добавления токена авторизации
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Интерсептор для обработки ошибок авторизации
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (data: LoginRequest): Promise<AxiosResponse<AuthResponse>> =>
    api.post('/auth/login', data),
  
  register: (data: RegisterRequest): Promise<AxiosResponse<AuthResponse>> =>
    api.post('/auth/register', data),
};

// User API
export const userAPI = {
  getCurrentUser: (): Promise<AxiosResponse<User>> =>
    api.get('/users/me'),
  
  getUserById: (id: number): Promise<AxiosResponse<User>> =>
    api.get(`/users/${id}`),
};

// Course API
export const courseAPI = {
  getAllCourses: (): Promise<AxiosResponse<Course[]>> =>
    api.get('/courses'),
  
  getCourseById: (id: number): Promise<AxiosResponse<Course>> =>
    api.get(`/courses/${id}`),
  
  createCourse: (data: CourseRequest): Promise<AxiosResponse<Course>> =>
    api.post('/courses', data),
  
  updateCourse: (id: number, data: CourseRequest): Promise<AxiosResponse<Course>> =>
    api.put(`/courses/${id}`, data),
  
  deleteCourse: (id: number): Promise<AxiosResponse<void>> =>
    api.delete(`/courses/${id}`),
  
  getMyCourses: (): Promise<AxiosResponse<Course[]>> =>
    api.get('/courses/my'),
  
  searchCourses: (query: string): Promise<AxiosResponse<Course[]>> =>
    api.get(`/courses/search?q=${encodeURIComponent(query)}`),
};

// Quiz API
export const quizAPI = {
  getQuizzesByCourse: (courseId: number): Promise<AxiosResponse<Quiz[]>> =>
    api.get(`/quizzes/course/${courseId}`),
  
  getQuizById: (id: number): Promise<AxiosResponse<Quiz>> =>
    api.get(`/quizzes/${id}`),
  
  createQuiz: (data: QuizRequest): Promise<AxiosResponse<Quiz>> =>
    api.post('/quizzes', data),
  
  updateQuiz: (id: number, data: QuizRequest): Promise<AxiosResponse<Quiz>> =>
    api.put(`/quizzes/${id}`, data),
  
  deleteQuiz: (id: number): Promise<AxiosResponse<void>> =>
    api.delete(`/quizzes/${id}`),
  
  getMyQuizzes: (): Promise<AxiosResponse<Quiz[]>> =>
    api.get('/quizzes/my'),
  
  startQuizAttempt: (quizId: number): Promise<AxiosResponse<QuizAttempt>> =>
    api.post(`/quizzes/${quizId}/start`),
  
  submitQuiz: (data: QuizSubmissionRequest): Promise<AxiosResponse<QuizAttempt>> =>
    api.post('/quizzes/submit', data),
  
  getMyAttempts: (): Promise<AxiosResponse<QuizAttempt[]>> =>
    api.get('/quizzes/attempts/my'),
  
  getQuizAttempts: (quizId: number): Promise<AxiosResponse<QuizAttempt[]>> =>
    api.get(`/quizzes/${quizId}/attempts`),
  
  getAttemptById: (attemptId: number): Promise<AxiosResponse<QuizAttempt>> =>
    api.get(`/quizzes/attempts/${attemptId}`),
};

export default api;
