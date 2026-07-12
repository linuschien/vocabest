import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { usePatchUser } from './use-patch-user';
import { vi, describe, it, expect } from 'vitest';
import { api } from '@/lib/api-client';
import React from 'react';

vi.mock('@/lib/api-client', () => ({
  api: {
    patch: vi.fn(),
  },
}));

describe('usePatchUser', () => {
  it('calls api.patch with correct arguments and invalidates queries on success', async () => {
    const queryClient = new QueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    
    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    vi.mocked(api.patch).mockResolvedValueOnce({} as any);

    const { result } = renderHook(() => usePatchUser(), { wrapper });

    await result.current.mutateAsync({
      id: 'user-123',
      payload: { targetLevel: 'SENIOR_HIGH', dailyTargetQuestions: 20 },
    });

    // Verify API call
    expect(api.patch).toHaveBeenCalledWith('/users/user-123', {
      targetLevel: 'SENIOR_HIGH',
      dailyTargetQuestions: 20,
    });

    // Verify cache invalidation
    expect(invalidateSpy).toHaveBeenCalled();
  });
});
