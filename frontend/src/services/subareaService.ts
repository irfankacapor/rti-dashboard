import { Subarea, SubareaFormData } from '@/types/subareas';

const API_BASE = '/api/v1/subareas';

export async function getSubareas(): Promise<Subarea[]> {
  const res = await fetch(API_BASE);
  if (!res.ok) throw new Error('Failed to fetch subareas');
  const data = await res.json();
  return data.map(mapSubareaFromApi);
}

export async function createSubarea(form: SubareaFormData): Promise<Subarea> {
  const res = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  });
  if (!res.ok) throw new Error('Failed to create subarea');
  return mapSubareaFromApi(await res.json());
}

export async function updateSubarea(id: string, updates: Partial<Subarea>): Promise<Subarea> {
  const res = await fetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updates),
  });
  if (!res.ok) throw new Error('Failed to update subarea');
  return mapSubareaFromApi(await res.json());
}

export async function deleteSubarea(id: string): Promise<void> {
  const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Failed to delete subarea');
}

function mapSubareaFromApi(api: any): Subarea {
  return {
    id: api.id,
    code: api.code,
    name: api.name,
    description: api.description,
    areaId: api.areaId,
    areaName: api.areaName,
    createdAt: new Date(api.createdAt),
  };
} 