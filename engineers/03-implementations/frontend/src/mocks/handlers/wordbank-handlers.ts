import { graphql, http, HttpResponse } from 'msw';
import { mockWordBanks } from '../fixtures';

const BASE_URL = '/api/v1';

export const wordBankHandlers = [
  graphql.query('listWordBanks', () => HttpResponse.json({ data: { listWordBanks: mockWordBanks } })),
  http.post(`${BASE_URL}/createWordBank`, () => HttpResponse.json({ success: true })),
  http.post(`${BASE_URL}/deleteWordBank`, () => HttpResponse.json({ success: true })),
];
