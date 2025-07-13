import { Indicator } from "@/types/indicators";
import { Dimension } from "@/types/dimensions";

export interface IndicatorValue {
  value: number;
  timeValue?: string; // e.g., "2023"
  timeType?: string; // "year", "month", "day"
  locationValue?: string; // e.g., "Burgenland"
  locationType?: string; // "state", "country", "city"
  customDimensions?: { [key: string]: string }; // e.g., {"Sector": "Public", "Gender": "Male"}
}

export interface ProcessedIndicator {
  id: string;
  name: string; // e.g., "KMU mit zumindest grundlegender Digitalisierungsintensität"
  description?: string;
  dimensions: Dimension[]; // Properly typed dimensions
  valueCount: number; // number of data points
  unit?: string;
  unitPrefix?: string;
  unitSuffix?: string;
  source?: string;
  subareaId?: string; // user assignment
  direction?: 'input' | 'output'; // user selection
  dataPoints?: IndicatorValue[]; // The dimensional data from frontend processing
}

export interface IndicatorBatchResponse {
  createdIndicators: Indicator[];
  totalFactRecords: number;
  message: string;
  warnings: string[];
}

// CSV File interface
export interface CsvFile {
  id: string;
  name: string;
  file: File;
  size: number;
  uploadedAt: Date;
  uploadStatus?: 'uploading' | 'uploaded' | 'error';
  jobId?: string;
  error?: string;
}

// Cell selection interface
export interface CellSelection {
  startRow: number;
  endRow: number;
  startCol: number;
  endCol: number;
  selectedCells: Array<{
    row: number;
    col: number;
    value: string;
  }>;
  selectionId: string;
}

// Dimension mapping interface
export interface DimensionMapping {
  id: string;
  selection: CellSelection;
  dimensionType: 'indicator_names' | 'indicator_values' | 'time' | 'locations' | 'unit' | 'source' | 'additional_dimension';
  mappingDirection: 'row' | 'column'; // NEW: Explicitly track mapping direction
  subType?: string; // e.g., 'year', 'month', 'state', 'city'
  customDimensionName?: string;
  uniqueValues?: string[];
  color: string;
}

// Data tuple interface for processing
export interface DataTuple {
  coordinates: {
    [key: string]: string;
  };
  value: string;
  sourceRow: number;
  sourceCol: number;
}

// CSV processing state interface
export interface CsvProcessingState {
  currentPhase: 'upload' | 'selection' | 'mapping' | 'encoding' | 'assignment';
  csvFile?: CsvFile;
  csvData?: string[][];
  dimensionMappings: DimensionMapping[];
  processedIndicators: ProcessedIndicator[];
  isLoading: boolean;
  error?: string;
}

export interface EncodingLocation {
  rowIndex: number;
  colIndex: number;
  dimensionType: string;
  originalValue: string; // Full cell value containing the issue
  previewFixed: string; // How it would look after fix
}

export interface EncodingIssue {
  problematicText: string;
  suggestedReplacement: string;
  issueType: 'KNOWN_ENCODING' | 'POTENTIAL_ENCODING';
  occurrenceCount: number;
  locations: EncodingLocation[];
}

export interface EncodingFixState {
  detectedIssues: EncodingIssue[];
  userReplacements: Map<string, string>; // problematicText -> userReplacement
  isScanning: boolean;
  hasIssues: boolean;
} 