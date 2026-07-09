import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api-client';

export interface UserResponse {
  id: string | null;
  email: string;
  role: string | null;
  targetLevel: string | null;
  learningStreak?: number;
  dailyTargetQuestions?: number;
}

export const whoamiKeys = {
  all: ['whoami'] as const,
};

export function useWhoami() {
  return useQuery({
    queryKey: whoamiKeys.all,
    queryFn: () => api.get<UserResponse>('/users:whoami'),
    retry: false, // Don't retry authentication checks repeatedly
  });
}
