import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { CsvProcessingStep } from '../CsvProcessingStep';
import { csvProcessingService } from '@/services/csvProcessingService';
import { getSubareas } from '@/services/subareaService';

// Mock the services
jest.mock('@/services/csvProcessingService');
jest.mock('@/services/subareaService');
jest.mock('@/lib/store/useWizardStore', () => ({
  useWizardStore: () => ({
    currentStep: 3,
    steps: [],
    isLoading: false,
    setCurrentStep: jest.fn(),
    nextStep: jest.fn(),
    prevStep: jest.fn(),
    setStepCompleted: jest.fn(),
    setStepValid: jest.fn(),
    setLoading: jest.fn(),
    canProceedToStep: jest.fn(() => true),
    resetWizard: jest.fn(),
  }),
}));

// Mock papaparse
jest.mock('papaparse', () => ({
  parse: jest.fn((csvText: string) => ({
    data: [
      ['Year', 'Value'],
      ['2023', '100']
    ],
    errors: []
  }))
}));

// Mock child components
jest.mock('../WizardContainer', () => ({
  WizardContainer: ({ children, title, subtitle }: any) => (
    <div data-testid="wizard-container">
      <h1 data-testid="wizard-title">{title}</h1>
      <p data-testid="wizard-subtitle">{subtitle}</p>
      {children}
    </div>
  ),
}));

jest.mock('../CsvUploadSection', () => ({
  CsvUploadSection: ({ onFileUploaded, onFileRemoved, uploadedFile, disabled }: any) => (
    <div data-testid="csv-upload-section">
      <button 
        data-testid="upload-button" 
        onClick={() => onFileUploaded && onFileUploaded(mockCsvFile)}
        disabled={disabled}
      >
        Upload CSV
      </button>
      {uploadedFile && (
        <div data-testid="uploaded-file">
          <span>{uploadedFile.name}</span>
          <button data-testid="remove-file" onClick={() => onFileRemoved && onFileRemoved(uploadedFile.id)}>
            Remove
          </button>
        </div>
      )}
    </div>
  ),
}));

jest.mock('../CsvTable', () => ({
  CsvTable: ({ data, onCellRangeSelect, existingMappings }: any) => (
    <div data-testid="csv-table">
      <div data-testid="csv-data-rows">{data?.length || 0} rows</div>
      <div data-testid="csv-mappings-count">{existingMappings?.length || 0} mappings</div>
      <button 
        data-testid="select-cell-range" 
        onClick={() => {
          if (onCellRangeSelect) {
            onCellRangeSelect(mockCellSelection, { target: document.createElement('button') });
          }
        }}
      >
        Select Range
      </button>
    </div>
  ),
}));

jest.mock('../DimensionMappingPopup', () => ({
  DimensionMappingPopup: ({ open, onConfirm, onCancel }: any) => (
    open ? (
      <div data-testid="dimension-mapping-popup">
        <button data-testid="confirm-mapping" onClick={() => onConfirm && onConfirm(mockDimensionMapping)}>
          Confirm
        </button>
        <button data-testid="cancel-mapping" onClick={() => onCancel && onCancel()}>
          Cancel
        </button>
      </div>
    ) : null
  ),
}));

jest.mock('../IndicatorAssignment', () => ({
  IndicatorAssignment: ({ indicators, subareas, onAssign, onSubmit, isLoading }: any) => (
    <div data-testid="indicator-assignment">
      <div data-testid="indicators-count">{indicators?.length || 0} indicators</div>
      <div data-testid="subareas-count">{subareas?.length || 0} subareas</div>
      <button 
        data-testid="assign-indicator" 
        onClick={() => onAssign && onAssign('indicator-1', 'subareaId', 'subarea-1')}
      >
        Assign
      </button>
      <button 
        data-testid="submit-indicators" 
        onClick={() => onSubmit && onSubmit()}
        disabled={isLoading}
      >
        Submit
      </button>
    </div>
  ),
}));

const mockSubareas = [
  {
    id: 'subarea-1',
    code: 'DIGITAL',
    name: 'Digitalization',
    description: 'Digital transformation indicators',
    areaId: 'area-1',
    areaName: 'Technology',
    createdAt: new Date()
  },
  {
    id: 'subarea-2',
    code: 'ECON',
    name: 'Economy',
    description: 'Economic indicators',
    areaId: 'area-2',
    areaName: 'Business',
    createdAt: new Date()
  }
];

const mockCsvFile = {
  id: 'file-1',
  name: 'test.csv',
  file: {
    text: jest.fn().mockResolvedValue('Year,Value\n2023,100'),
    name: 'test.csv',
    type: 'text/csv'
  }
};

const mockCellSelection = {
  startRow: 1,
  startCol: 0,
  endRow: 1,
  endCol: 1,
  data: ['2023', '100']
};

const mockDimensionMapping = {
  id: 'mapping-1',
  dimensionType: 'indicator_values',
  selection: mockCellSelection,
  mappingDirection: 'row' as const,
  color: '#2196f3',
  uniqueValues: ['100']
};

const austrianCsvData = [
  ['Year', 'Bundesland', 'KMU mit zumindest grundlegender Digitalisierungsintensität', 'Unternehmen mit elektronischem Informationsaustausch'],
  ['2023', 'Burgenland', '49', '32'],
  ['2023', 'Kärnten', '54', '31'],
  ['2023', 'Niederösterreich', '51', '41'],
  ['2023', 'Oberösterreich', '53', '38'],
  ['2023', 'Salzburg', '56', '42'],
  ['2023', 'Steiermark', '52', '39'],
  ['2023', 'Tirol', '55', '40'],
  ['2023', 'Vorarlberg', '58', '45'],
  ['2023', 'Wien', '60', '48']
];

describe('CsvProcessingStep', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (getSubareas as jest.Mock).mockResolvedValue(mockSubareas);
    (csvProcessingService.submitProcessedIndicators as jest.Mock) = jest.fn().mockResolvedValue({
      createdIndicators: [{ id: 'indicator-1', name: 'Test Indicator' }],
      totalFactRecords: 10
    });
  });

  it('renders upload phase initially', async () => {
    render(<CsvProcessingStep />);
    
    await waitFor(() => {
      expect(screen.getByTestId('wizard-container')).toBeInTheDocument();
    });
    
    expect(screen.getByTestId('wizard-title')).toHaveTextContent('CSV Data Processing');
    expect(screen.getByTestId('csv-upload-section')).toBeInTheDocument();
    expect(screen.getByTestId('upload-button')).toBeInTheDocument();
  });

  it('loads subareas on mount', async () => {
    render(<CsvProcessingStep />);
    
    await waitFor(() => {
      expect(getSubareas).toHaveBeenCalled();
    });
  });

  it('handles file upload and transitions to selection phase', async () => {
    render(<CsvProcessingStep />);
    
    await waitFor(() => {
      expect(screen.getByTestId('upload-button')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByTestId('upload-button'));
    
    await waitFor(() => {
      expect(screen.getByTestId('csv-table')).toBeInTheDocument();
    });
    
    expect(
      screen.getByText((content) =>
        content.includes('Select cells in the CSV table to map them to different dimensions')
      )
    ).toBeInTheDocument();
  });

  it('shows CSV data in selection phase', async () => {
    render(<CsvProcessingStep />);
    
    await waitFor(() => {
      expect(screen.getByTestId('upload-button')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByTestId('upload-button'));
    
    await waitFor(() => {
      expect(screen.getByTestId('csv-table')).toBeInTheDocument();
    });
    
    expect(screen.getByTestId('csv-data-rows')).toHaveTextContent('2 rows');
    expect(screen.getByTestId('csv-mappings-count')).toHaveTextContent('0 mappings');
  });

  it('shows process mappings button in selection phase', async () => {
    render(<CsvProcessingStep />);
    
    await waitFor(() => {
      expect(screen.getByTestId('upload-button')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByTestId('upload-button'));
    
    await waitFor(() => {
      expect(screen.getByTestId('csv-table')).toBeInTheDocument();
    });
    
    expect(screen.getByText('Process Mappings')).toBeInTheDocument();
  });

  it('shows error message when file processing fails', async () => {
    // Mock papaparse to return errors
    const mockPapaparse = require('papaparse');
    mockPapaparse.parse.mockReturnValue({
      data: [],
      errors: [{ message: 'Invalid CSV format' }]
    });
    
    render(<CsvProcessingStep />);
    
    await waitFor(() => {
      expect(screen.getByTestId('upload-button')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByTestId('upload-button'));
    
    await waitFor(() => {
      expect(screen.getByText(/CSV parsing errors/)).toBeInTheDocument();
    });
  });

  // Note: Complex mapping validation and assignment phase tests require more detailed mocking
  // of the coordinate processor utilities and validation logic. These tests demonstrate
  // the core component functionality including file upload, phase transitions, and error handling.
}); 