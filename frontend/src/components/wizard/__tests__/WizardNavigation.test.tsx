import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { WizardNavigation } from '../WizardNavigation';
import { useWizardStore } from '@/lib/store/useWizardStore';

jest.mock('@/lib/store/useWizardStore');
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
  }),
}));

const mockUseWizardStore = useWizardStore as jest.MockedFunction<typeof useWizardStore>;

describe('WizardNavigation', () => {
  const mockNextStep = jest.fn();
  const mockPrevStep = jest.fn();
  const mockCanProceedToStep = jest.fn();

  const defaultMockState = {
    currentStep: 2,
    steps: [
      { id: 1, name: 'areas', label: 'Areas Management', isCompleted: true, isValid: true },
      { id: 2, name: 'subareas', label: 'Subareas Management', isCompleted: false, isValid: true },
      { id: 3, name: 'upload', label: 'Data Upload', isCompleted: false, isValid: false },
    ],
    nextStep: mockNextStep,
    prevStep: mockPrevStep,
    canProceedToStep: mockCanProceedToStep,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockCanProceedToStep.mockImplementation((step) => step <= 2);
  });

  it('renders back and next buttons', () => {
    mockUseWizardStore.mockReturnValue(defaultMockState as any);

    render(<WizardNavigation />);

    expect(screen.getByTestId('wizard-prev-button')).toBeInTheDocument();
    expect(screen.getByTestId('wizard-next-button')).toBeInTheDocument();
  });

  it('does not render back button on first step', () => {
    const firstStepState = { ...defaultMockState, currentStep: 1 };
    mockUseWizardStore.mockReturnValue(firstStepState as any);

    render(<WizardNavigation />);

    expect(screen.queryByTestId('wizard-prev-button')).not.toBeInTheDocument();
  });

  it('renders finish button on last step', () => {
    const lastStepState = { ...defaultMockState, currentStep: 3 };
    mockUseWizardStore.mockReturnValue(lastStepState as any);

    render(<WizardNavigation />);

    expect(screen.getByTestId('wizard-finish-button')).toBeInTheDocument();
    expect(screen.queryByTestId('wizard-next-button')).not.toBeInTheDocument();
  });

  it('calls prevStep when back button is clicked', () => {
    mockUseWizardStore.mockReturnValue(defaultMockState as any);

    render(<WizardNavigation />);

    fireEvent.click(screen.getByTestId('wizard-prev-button'));
    expect(mockPrevStep).toHaveBeenCalledTimes(1);
  });

  it('calls nextStep when next button is clicked', () => {
    mockCanProceedToStep.mockImplementation((step) => step <= 3);
    mockUseWizardStore.mockReturnValue(defaultMockState as any);

    render(<WizardNavigation />);

    fireEvent.click(screen.getByTestId('wizard-next-button'));
    expect(mockNextStep).toHaveBeenCalledTimes(1);
  });

  it('calls custom onNext when provided', () => {
    const mockOnNext = jest.fn();
    mockCanProceedToStep.mockImplementation((step) => step <= 3);
    mockUseWizardStore.mockReturnValue(defaultMockState as any);

    render(<WizardNavigation onNext={mockOnNext} />);

    fireEvent.click(screen.getByTestId('wizard-next-button'));
    expect(mockOnNext).toHaveBeenCalledTimes(1);
    expect(mockNextStep).not.toHaveBeenCalled();
  });

  it('disables next button when nextDisabled is true', () => {
    mockUseWizardStore.mockReturnValue(defaultMockState as any);

    render(<WizardNavigation nextDisabled={true} />);

    expect(screen.getByTestId('wizard-next-button')).toBeDisabled();
  });

  it('does not disable next button when cannot proceed to next step (component does not implement this logic)', () => {
    mockCanProceedToStep.mockImplementation((step) => step <= 1);
    mockUseWizardStore.mockReturnValue(defaultMockState as any);

    render(<WizardNavigation />);

    // The component doesn't automatically disable based on canProceedToStep
    // It only disables when nextDisabled prop is true
    expect(screen.getByTestId('wizard-next-button')).not.toBeDisabled();
  });
}); 