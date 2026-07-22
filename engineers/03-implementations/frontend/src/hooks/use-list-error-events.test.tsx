import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useListErrorEvents } from './use-list-error-events';
import { vi, describe, it, expect } from 'vitest';
import { request } from 'graphql-request';
import React from 'react';

vi.mock('graphql-request', () => ({
  request: vi.fn(),
  gql: (strs: any) => strs[0],
}));

describe('useListErrorEvents', () => {
  it('calls graphql request with correct arguments', async () => {
    const queryClient = new QueryClient();
    
    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const mockResponse = {
      listErrorEvents: {
        content: [{ id: 'error-1' }],
        totalElements: 1
      }
    };

    vi.mocked(request).mockResolvedValueOnce(mockResponse as any);

    const { result } = renderHook(
      () => useListErrorEvents({ userId: 'user-123', page: 0, size: 10 }), 
      { wrapper }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(request).toHaveBeenCalled();
    const calls = vi.mocked(request).mock.calls[0];
    expect(calls[2]).toEqual({
      filter: { userId: 'user-123', page: 0, size: 10 }
    });
    
    expect(result.current.data).toEqual(mockResponse.listErrorEvents);
  });
});
