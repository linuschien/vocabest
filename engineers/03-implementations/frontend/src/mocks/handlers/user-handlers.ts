import { graphql, http, HttpResponse } from 'msw';
import { mockUsers } from '../fixtures';

const BASE_URL = '/api/v1';

export const userHandlers = [
  graphql.query('listUsers', () => HttpResponse.json({ data: { listUsers: mockUsers } })),
  http.get(`${BASE_URL}/getUserById`, () => HttpResponse.json({ id: '1', name: 'User' })),
  http.post(`${BASE_URL}/onboardUser`, () => HttpResponse.json({ success: true })),
];
