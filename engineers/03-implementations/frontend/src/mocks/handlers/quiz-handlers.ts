import { graphql, http, HttpResponse } from 'msw';
import { mockQuizQuestions } from '../fixtures';

const BASE_URL = '/api/v1';

export const quizHandlers = [
  graphql.query('listQuizQuestions', () => HttpResponse.json({ data: { listQuizQuestions: mockQuizQuestions } })),
  http.get(`${BASE_URL}/getNextQuestion`, () => HttpResponse.json({ id: '1', text: 'Q1' })),
  http.post(`${BASE_URL}/submitAnswer`, () => HttpResponse.json({ success: true })),
  http.post(`${BASE_URL}/createQuizQuestion`, () => HttpResponse.json({ success: true })),
  http.post(`${BASE_URL}/deleteQuizQuestion`, () => HttpResponse.json({ success: true })),
];
