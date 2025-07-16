import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useDashboardWithRelationships } from '../../hooks/useDashboardWithRelationships';
import { apiClient } from '../../lib/api';
import React from 'react';

const queryClient = new QueryClient();
function wrapper({ children }: { children: React.ReactNode }) {
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}

describe('useDashboardWithRelationships', () => {
  const mockResponse = {
    areas: [{ id: 1, name: 'Area 1' }],
    subareas: [{ id: 10, name: 'Subarea 1' }],
    goals: [{ id: 100, name: 'Goal 1' }],
    goalGroups: [{ id: 1000, name: 'Group 1' }],
    relationships: {
      goalToSubareas: { '100': ['10'] },
      subareaToGoals: { '10': ['100'] },
    },
    lastUpdated: '2024-01-01T00:00:00Z',
  };

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('returns loading state initially', async () => {
    jest.spyOn(apiClient, 'get').mockImplementation(() => Promise.resolve({ data: mockResponse }));
    const { result } = renderHook(() => useDashboardWithRelationships(), { wrapper });
    expect(result.current.isLoading).toBe(true);
    await waitFor(() => !result.current.isLoading);
  });
});

export {}; 