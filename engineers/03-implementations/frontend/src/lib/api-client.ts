const API_BASE_URL = '/api/v1';

async function fetchApi<T>(path: string, options: RequestInit = {}): Promise<T> {
  const url = `${API_BASE_URL}${path}`;
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);
    throw {
      status: response.status,
      message: errorData?.message || response.statusText || 'API Error',
      data: errorData,
    };
  }

  // Handle 204 No Content or empty responses
  if (response.status === 204) {
    return null as unknown as T;
  }
  return response.json();
}

export const api = {
  get: <T>(path: string, options?: any) => {
    const qs = options?.params ? '?' + new URLSearchParams(options.params).toString() : '';
    return fetchApi<T>(path + qs, { method: 'GET' });
  },
  post: <T>(path: string, data?: any) => fetchApi<T>(path, { method: 'POST', body: JSON.stringify(data) }),
  put: <T>(path: string, data?: any) => fetchApi<T>(path, { method: 'PUT', body: JSON.stringify(data) }),
  patch: <T>(path: string, data?: any) => fetchApi<T>(path, { method: 'PATCH', body: JSON.stringify(data) }),
  delete: <T>(path: string) => fetchApi<T>(path, { method: 'DELETE' }),
};
