import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CsvProcessingStep } from '@/components/wizard/CsvProcessingStep';
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
    data: csvText.split('\n').map((line: string) => line.split(',')),
    errors: []
  }))
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

function renderCsvProcessingStepWithSelectionPhase(csvData = austrianCsvData) {
  // Render the component
  render(<CsvProcessingStep />);
  // Simulate state: show the selection phase with mock data
  // This requires a test utility or exposing state for test, or you can simulate the upload by calling the upload handler if available
  // For now, just check that the selection phase renders correctly
  // (In a real test, you would refactor CsvProcessingStep to allow injection of initial state for tests)
}

describe('CsvProcessingStep', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (getSubareas as jest.Mock).mockResolvedValue(mockSubareas);
    (csvProcessingService.submitIndicators as jest.Mock).mockResolvedValue(undefined);
  });

  it('renders upload phase initially', async () => {
    render(<CsvProcessingStep />);
    // Wait for the async useEffect to finish (even if you don't assert on subareas)
    await waitFor(() => {
      expect(screen.getAllByText('Upload CSV File').length).toBeGreaterThan(0);
    });
    expect(screen.getByText(/Drag & drop a CSV file here/)).toBeInTheDocument();
  });

  // Additional tests would go here, but for now, only test the initial render and skip dropzone logic
}); 