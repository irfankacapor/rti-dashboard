export interface Area {
  id: string; 
  code: string;
  name: string;
  description: string;
  isDefault: boolean;
  createdAt: Date;
  subareaCount?: number;
}

export interface AreaApiResponse {
  id: string; 
  code: string;
  name: string;
  description: string;
  isDefault: boolean;
  createdAt: Date;
  subareaCount?: number;
}

export interface AreaFormData {
  name: string;
  description: string;
} 