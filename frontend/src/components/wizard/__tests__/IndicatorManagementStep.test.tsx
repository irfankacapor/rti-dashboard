// Mock the stores at the top level
let mockStore: any = {};

jest.mock('@/store/wizardStore', () => ({
  useWizardStore: () => mockStore,
}));

jest.mock('@/lib/store/useWizardStore', () => ({
  useWizardStore: (selector: any) => {
    const state = {
      setStepCompleted: jest.fn(),
      setStepValid: jest.fn(),
    };
    return selector ? selector(state) : state;
  },
}));

import React from 'react';
import { render } from '@testing-library/react';
import { IndicatorManagementStep } from '../IndicatorManagementStep';

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
  const baseStore = {
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
    fetchManagedIndicators: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockStore = { ...baseStore };
  });

  it('renders the component with all expected elements', () => {
    const { getByText, getByTestId } = render(<IndicatorManagementStep onNavigateToStep={mockOnNavigateToStep} />);
    expect(getByText('Indicator Overview')).toBeInTheDocument();
    expect(getByText('Add Manual')).toBeInTheDocument();
    expect(getByTestId('add-more-csv')).toBeInTheDocument();
    expect(getByText('Total Indicators')).toBeInTheDocument();
    expect(getByTestId('add-more-csv')).toBeInTheDocument();
  });

  it('shows loading state when loading indicators', () => {
    mockStore = { ...baseStore, isLoadingIndicators: true };
    const { getByTestId } = render(<IndicatorManagementStep onNavigateToStep={mockOnNavigateToStep} />);
    expect(getByTestId('circular-progress')).toBeInTheDocument();
  });
}); 