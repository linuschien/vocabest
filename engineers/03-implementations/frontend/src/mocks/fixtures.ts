// Global data fixtures for MSW testing

export const mockUsers: any[] = [];
export const mockQuizQuestions: any[] = [];
export const mockWordBanks: any[] = [];
export const mockDailyProgress: any[] = [];

export function resetMockData() {
  mockUsers.splice(0, mockUsers.length);
  mockQuizQuestions.splice(0, mockQuizQuestions.length);
  mockWordBanks.splice(0, mockWordBanks.length);
  mockDailyProgress.splice(0, mockDailyProgress.length);
}
