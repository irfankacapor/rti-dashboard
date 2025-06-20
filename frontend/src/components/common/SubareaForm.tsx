import React from 'react';
import { SubareaFormData } from '@/types/subareas';

interface SubareaFormProps {
  initialData?: SubareaFormData;
  areas: { id: string; name: string }[];
  onSave: (data: SubareaFormData) => void;
  onCancel: () => void;
}

export const SubareaForm: React.FC<SubareaFormProps> = () => {
  return null; // To be implemented if needed for modal form usage
}; 