import React from 'react';
import { render, screen } from '@testing-library/react';
import { IndicatorManagementStep } from '../IndicatorManagementStep';

// Mock the stores
jest.mock('@/store/wizardStore', () => ({
  useWizardStore: () => ({
    dirtyIndicators: [],
    dirtySubareas: [],
    updateManagedIndicator: jest.fn(),
    addManualIndicator: jest.fn(),
    deleteManagedIndicator: jest.fn(),
    bulkUpdateIndicators: jest.fn(),
    bulkDeleteIndicators: jest.fn(),
    validateIndicatorData: jest.fn(() => ({ isValid: true, errors: [] })),
    isLoadingIndicators: false,
    isSaving: false,
  }),
}));

jest.mock('@/lib/store/useWizardStore', () => ({
  useWizardStore: () => ({
    setStepCompleted: jest.fn(),
    setStepValid: jest.fn(),
  }),
}));

// Mock the child components to avoid complex rendering
jest.mock('../IndicatorTable', () => ({
  IndicatorTable: () => <div data-testid="indicator-table">Indicator Table</div>,
}));

jest.mock('../AddIndicatorForm', () => ({
  AddIndicatorForm: () => <div data-testid="add-indicator-form">Add Indicator Form</div>,
}));

jest.mock('../BulkIndicatorActions', () => ({
  BulkIndicatorActions: () => <div data-testid="bulk-actions">Bulk Actions</div>,
}));

jest.mock('../AddMoreCsvSection', () => ({
  AddMoreCsvSection: () => <div data-testid="add-more-csv">Add More CSV</div>,
}));

describe('IndicatorManagementStep', () => {
  const mockOnNavigateToStep = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the indicator management step with basic elements', () => {
    render(<IndicatorManagementStep onNavigateToStep={mockOnNavigateToStep} />);
    
    expect(screen.getByText('Indicator Overview')).toBeInTheDocument();
    expect(screen.getByText('Add Manual')).toBeInTheDocument();
    expect(screen.getByText('Add More CSV')).toBeInTheDocument();
    expect(screen.getByText('Total Indicators')).toBeInTheDocument();
  });

  it('shows loading state when loading indicators', () => {
    // Mock with loading state
    jest.doMock('@/store/wizardStore', () => ({
      useWizardStore: () => ({
        dirtyIndicators: [],
        dirtySubareas: [],
        updateManagedIndicator: jest.fn(),
        addManualIndicator: jest.fn(),
        deleteManagedIndicator: jest.fn(),
        bulkUpdateIndicators: jest.fn(),
        bulkDeleteIndicators: jest.fn(),
        validateIndicatorData: jest.fn(() => ({ isValid: true, errors: [] })),
        isLoadingIndicators: true,
        isSaving: false,
      }),
    }));

    render(<IndicatorManagementStep onNavigateToStep={mockOnNavigateToStep} />);
    
    // Should show loading indicator
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
}); 