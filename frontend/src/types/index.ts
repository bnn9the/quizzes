export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN'
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface Course {
  id: number;
  title: string;
  description: string;
  teacher: User;
  createdAt: string;
  updatedAt: string;
}

export interface CourseRequest {
  title: string;
  description: string;
}

export interface Quiz {
  id: number;
  title: string;
  description: string;
  course: Course;
  maxAttempts: number;
  timeLimitMinutes: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  questions: Question[];
}

export interface Question {
  id: number;
  text: string;
  type: QuestionType;
  points: number;
  answerOptions: AnswerOption[];
}

export enum QuestionType {
  SINGLE_CHOICE = 'SINGLE_CHOICE',
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
  TRUE_FALSE = 'TRUE_FALSE',
  TEXT = 'TEXT'
}

export interface AnswerOption {
  id: number;
  text: string;
  isCorrect: boolean;
}

export interface QuizRequest {
  title: string;
  description: string;
  courseId: number;
  maxAttempts: number;
  timeLimitMinutes: number;
  isActive: boolean;
  questions: QuestionRequest[];
}

export interface QuestionRequest {
  text: string;
  type: QuestionType;
  points: number;
  answerOptions: AnswerOptionRequest[];
}

export interface AnswerOptionRequest {
  text: string;
  isCorrect: boolean;
}

export interface QuizAttempt {
  id: number;
  quiz: Quiz;
  student: User;
  startTime: string;
  endTime?: string;
  score?: number;
  maxScore: number;
  isCompleted: boolean;
  studentAnswers: StudentAnswer[];
}

export interface StudentAnswer {
  id: number;
  question: Question;
  selectedOptions: AnswerOption[];
  textAnswer?: string;
}

export interface QuizSubmissionRequest {
  quizId: number;
  answers: StudentAnswerRequest[];
}

export interface StudentAnswerRequest {
  questionId: number;
  selectedOptionIds: number[];
  textAnswer?: string;
}

export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path: string;
}
