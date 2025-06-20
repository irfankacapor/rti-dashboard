export interface Subarea {
  id: string;
  code: string;
  name: string;
  description: string;
  areaId: string;
  areaName?: string; // For display purposes
  createdAt: Date;
}

export interface SubareaFormData {
  name: string;
  description: string;
  areaId: string; // Required, either real area or default area
} 