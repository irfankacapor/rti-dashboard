import { UnitResponse } from '@/types/indicators';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || '';

export const unitService = {
  getGroupedUnits: async (): Promise<Record<string, UnitResponse[]>> => {
    const response = await fetch(`${API_BASE}/units`);
    if (!response.ok) {
      throw new Error('Failed to fetch units');
    }
    return response.json();
  },
}; 