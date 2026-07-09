import { userHandlers } from './handlers/user-handlers';
import { quizHandlers } from './handlers/quiz-handlers';
import { wordBankHandlers } from './handlers/wordbank-handlers';
import { progressHandlers } from './handlers/progress-handlers';

export const handlers = [
  ...userHandlers,
  ...quizHandlers,
  ...wordBankHandlers,
  ...progressHandlers,
];

// Re-export shared fixtures and resets so tests can import from here if needed
export * from './fixtures';
