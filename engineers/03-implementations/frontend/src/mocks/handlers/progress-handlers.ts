import { http, HttpResponse, graphql } from 'msw';

const BASE_URL = '/api/v1';

export const progressHandlers = [
  graphql.query('listDailyProgresses', () => HttpResponse.json({
    data: {
      listDailyProgresses: []
    }
  })),
  http.get(`${BASE_URL}/getDailyProgressById`, () => HttpResponse.json({ id: '1', progress: 100 })),
];
