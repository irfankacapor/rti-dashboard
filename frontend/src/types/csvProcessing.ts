export interface CsvFile {
  id: string;
  file: File;
  name: string;
  uploadStatus: 'uploading' | 'uploaded' | 'error';
  jobId?: string;
  data?: string[][]; // Raw CSV data as 2D array
  error?: string;
}

export interface CellSelection {
  startRow: number;
  endRow: number;
  startCol: number;
  endCol: number;
  selectedCells: { row: number; col: number; value: string }[];
  selectionId: string;
}

export interface DimensionMapping {
  id: string;
  selection: CellSelection;
  dimensionType: 'time' | 'locations' | 'indicator_names' | 'indicator_values' | 'source' | 'unit' | 'goals' | 'additional_dimension';
  subType?: string; // year/month/day for time, country/state/city for locations
  customDimensionName?: string; // for additional_dimension
  uniqueValues: string[]; // extracted unique values from selection
  color: string; // for visual highlighting
}

export interface DataTuple {
  coordinates: { [dimensionType: string]: string };
  value: string;
  sourceRow: number;
  sourceCol: number;
}

export interface ProcessedIndicator {
  id: string;
  name: string; // e.g., "KMU mit zumindest grundlegender Digitalisierungsintensität"
  dimensions: string[]; // e.g., ['time', 'locations']
  valueCount: number; // number of data points
  unit?: string;
  source?: string;
  subareaId?: string; // user assignment
  direction?: 'input' | 'output'; // user selection
}

export interface CsvProcessingState {
  currentPhase: 'upload' | 'selection' | 'mapping' | 'assignment';
  csvFile?: CsvFile;
  csvData?: string[][];
  dimensionMappings: DimensionMapping[];
  processedIndicators: ProcessedIndicator[];
  isLoading: boolean;
  error?: string;
}

export interface CsvUploadResponse {
  jobId: string;
  message: string;
}

export interface CsvPreviewResponse {
  data: string[][];
  headers: string[];
  totalRows: number;
  totalColumns: number;
}

export interface DimensionMappingRequest {
  jobId: string;
  mappings: DimensionMapping[];
}

export interface ProcessedIndicatorsResponse {
  indicators: ProcessedIndicator[];
}

export interface BatchIndicatorsRequest {
  indicators: ProcessedIndicator[];
} 