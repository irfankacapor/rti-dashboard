import { Subarea, SubareaFormData } from '@/types/subareas';
import { slugify } from '@/utils/slugify';

const API_BASE = `${process.env.NEXT_PUBLIC_API_URL}/subareas`;

function mapSubareaFromApi(subarea: Subarea): Subarea {
  return {
    id: String(subarea.id),
    code: subarea.code,
    name: subarea.name,
    description: subarea.description,
    areaId: String(subarea.areaId),
    areaName: subarea.areaName,
    createdAt: new Date(subarea.createdAt),
  };
}

export async function getSubareas(): Promise<Subarea[]> {
  const res = await fetch(API_BASE);
  
  if (!res.ok) {
    const errorText = await res.text().catch(() => 'Unknown error');
    console.error('Failed to fetch subareas:', res.status, errorText);
    throw new Error(`Failed to fetch subareas: ${res.status} - ${errorText}`);
  }
  
  const data = await res.json();
  return data.map(mapSubareaFromApi);
}

export async function createSubarea(form: SubareaFormData): Promise<Subarea> {
  const requestBody = {
    ...form,
    code: slugify(form.name),
  };

  const res = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestBody),
  });
  
  if (!res.ok) {
    const errorText = await res.text().catch(() => 'Unknown error');
    console.error('Failed to create subarea:', res.status, errorText);
    throw new Error(`Failed to create subarea: ${res.status} - ${errorText}`);
  }
  
  return mapSubareaFromApi(await res.json());
}

export async function updateSubarea(id: string, updates: Partial<SubareaFormData>): Promise<Subarea> {
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
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(errorData.message || `Failed to delete subarea: ${res.statusText}`);
  }
}

export async function deleteSubareaWithData(id: string): Promise<void> {
  const res = await fetch(`${API_BASE}/${id}/with-data`, { method: 'DELETE' });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(errorData.message || `Failed to delete subarea with data: ${res.statusText}`);
  }
} 