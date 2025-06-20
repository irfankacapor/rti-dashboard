import { 
  CsvUploadResponse, 
  CsvPreviewResponse, 
  DimensionMappingRequest, 
  ProcessedIndicatorsResponse,
  BatchIndicatorsRequest,
  DimensionMapping 
} from '@/types/csvProcessing';

const API_BASE = '/api/v1';

export const csvProcessingService = {
  // Upload CSV file
  uploadCsv: async (file: File): Promise<CsvUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${API_BASE}/upload-csv`, {
      method: 'POST',
      body: formData
    });
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to upload CSV: ${error}`);
    }
    
    return response.json();
  },
  
  // Get CSV preview data
  getCsvPreview: async (jobId: string): Promise<CsvPreviewResponse> => {
    const response = await fetch(`${API_BASE}/uploads/${jobId}/csv-preview`);
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to get CSV preview: ${error}`);
    }
    
    return response.json();
  },
  
  // Submit dimension mappings and process data
  processDimensions: async (jobId: string, mappings: DimensionMapping[]): Promise<ProcessedIndicatorsResponse> => {
    const request: DimensionMappingRequest = { jobId, mappings };
    
    const response = await fetch(`${API_BASE}/uploads/${jobId}/dimension-mapping`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to process dimensions: ${error}`);
    }
    
    return response.json();
  },
  
  // Submit final indicators with subarea assignments
  submitIndicators: async (indicators: any[]): Promise<void> => {
    const request: BatchIndicatorsRequest = { indicators };
    
    const response = await fetch(`${API_BASE}/indicators/batch`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to submit indicators: ${error}`);
    }
  }
}; 