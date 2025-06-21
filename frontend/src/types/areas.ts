export interface Area {
  id: string; // Use UUID for frontend
  code: string; // Auto-generated from name
  name: string;
  description: string;
  isDefault: boolean; // Track if this is the default area
  createdAt: Date;
  subareaCount?: number; // Number of subareas in this area
}

export interface AreaFormData {
  name: string;
  description: string;
} 