import { Area, AreaApiResponse, AreaFormData } from '@/types/areas';
import { slugify } from '@/utils/slugify';

const API_BASE = `${process.env.NEXT_PUBLIC_API_URL}/areas`;

function mapAreaFromApi(apiArea: AreaApiResponse): Area {
  return {
    id: String(apiArea.id),
    code: apiArea.code,
    name: apiArea.name,
    description: apiArea.description,
    isDefault: false,
    createdAt: new Date(apiArea.createdAt),
    subareaCount: apiArea.subareaCount || 0,
  };
}

export async function getAreas(): Promise<Area[]> {
  const res = await fetch(API_BASE);
  if (!res.ok) {
    console.error('Failed to fetch areas:', res.status, await res.text().catch(() => ''));
    throw new Error('Failed to fetch areas from the server.');
  }
  const data = await res.json();
  return data.map(mapAreaFromApi);
}

export async function createArea(formData: AreaFormData): Promise<Area> {
  const requestBody = {
    ...formData,
    code: slugify(formData.name),
  };

  const res = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestBody),
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({ message: 'Failed to create area.' }));
    throw new Error(errorData.message || 'An unknown error occurred while creating the area.');
  }

  return mapAreaFromApi(await res.json());
}

export async function updateArea(id: string, formData: Partial<AreaFormData>): Promise<Area> {
  const res = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(formData),
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({ message: 'Failed to update area.' }));
    throw new Error(errorData.message || 'An unknown error occurred while updating the area.');
  }

  return mapAreaFromApi(await res.json());
}

export async function deleteArea(id: string): Promise<void> {
  const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
  if (!res.ok) {
    const errorText = await res.text();
    try {
      const errorData = JSON.parse(errorText);
      throw new Error(errorData.message || 'Failed to delete the area.');
    } catch {
      throw new Error(errorText || 'Failed to delete the area.');
    }
  }
} 