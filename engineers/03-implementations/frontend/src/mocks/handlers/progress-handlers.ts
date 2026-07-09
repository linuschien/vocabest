import { http, HttpResponse } from 'msw';

const BASE_URL = '/api/v1';

export const progressHandlers = [
  http.get(`${BASE_URL}/getDailyProgressById`, () => HttpResponse.json({ id: '1', progress: 100 })),
];
