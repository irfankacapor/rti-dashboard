import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { WizardStepper } from '../WizardStepper';
import { useWizardStore } from '@/lib/store/useWizardStore';

jest.mock('@/lib/store/useWizardStore');

const mockUseWizardStore = useWizardStore as jest.MockedFunction<typeof useWizardStore>;

describe('WizardStepper', () => {
  const mockSetCurrentStep = jest.fn();
  const mockCanProceedToStep = jest.fn();

  const defaultMockState = {
    currentStep: 1,
    steps: [
      { id: 1, name: 'areas', label: 'Areas Management', isCompleted: false, isValid: true },
      { id: 2, name: 'subareas', label: 'Subareas Management', isCompleted: false, isValid: false },
      { id: 3, name: 'upload', label: 'Data Upload', isCompleted: false, isValid: false },
    ],
    setCurrentStep: mockSetCurrentStep,
    canProceedToStep: mockCanProceedToStep,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockCanProceedToStep.mockImplementation((step) => step <= 1);
  });

  it('renders wizard stepper with steps', () => {
    mockUseWizardStore.mockReturnValue(defaultMockState as any);

    render(<WizardStepper />);

    expect(screen.getByTestId('wizard-stepper')).toBeInTheDocument();
    expect(screen.getByTestId('stepper')).toBeInTheDocument();
    expect(screen.getByText('Areas Management')).toBeInTheDocument();
    expect(screen.getByText('Subareas Management')).toBeInTheDocument();
  });

  it('shows progress bar with correct progress', () => {
    const stateWithCompleted = {
      ...defaultMockState,
      steps: [
        { id: 1, name: 'areas', label: 'Areas Management', isCompleted: true, isValid: true },
        { id: 2, name: 'subareas', label: 'Subareas Management', isCompleted: false, isValid: false },
        { id: 3, name: 'upload', label: 'Data Upload', isCompleted: false, isValid: false },
      ],
    };
    
    mockUseWizardStore.mockReturnValue(stateWithCompleted as any);

    render(<WizardStepper />);

    const progressBar = screen.getByTestId('progress-bar');
    expect(progressBar).toHaveAttribute('aria-valuenow', '33'); // 1/3 * 100
    expect(screen.getByText('1/3')).toBeInTheDocument();
  });

  it('shows step as completed when isCompleted is true', () => {
    const stateWithCompleted = {
      ...defaultMockState,
      steps: [
        { id: 1, name: 'areas', label: 'Areas Management', isCompleted: true, isValid: true },
        { id: 2, name: 'subareas', label: 'Subareas Management', isCompleted: false, isValid: false },
      ],
    };
    mockUseWizardStore.mockReturnValue(stateWithCompleted as any);
    render(<WizardStepper />);
    // The completed step should have the label and the button should not be disabled
    expect(screen.getByText('Areas Management')).toBeInTheDocument();
    expect(screen.getByTestId('step-button-1')).not.toBeDisabled();
  });

  it('shows step button as disabled when cannot access', () => {
    mockCanProceedToStep.mockImplementation((step) => step === 1);
    mockUseWizardStore.mockReturnValue(defaultMockState as any);
    render(<WizardStepper />);
    expect(screen.getByTestId('step-button-2')).toBeDisabled();
  });

  it('calls setCurrentStep when accessible step is clicked', () => {
    mockUseWizardStore.mockReturnValue(defaultMockState as any);
    render(<WizardStepper />);
    fireEvent.click(screen.getByTestId('step-button-1'));
    expect(mockSetCurrentStep).toHaveBeenCalledWith(1);
  });

  it('does not call setCurrentStep when inaccessible step is clicked', () => {
    mockCanProceedToStep.mockImplementation((step) => step === 1);
    mockUseWizardStore.mockReturnValue(defaultMockState as any);
    render(<WizardStepper />);
    fireEvent.click(screen.getByTestId('step-button-2'));
    expect(mockSetCurrentStep).not.toHaveBeenCalledWith(2);
  });
}); 