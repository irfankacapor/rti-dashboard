jest.mock('@/lib/store/useWizardStore', () => ({
  useWizardStore: (selector: any) => {
    const state = {
      setStepValid: jest.fn(),
      setStepCompleted: jest.fn(),
      currentStep: 1,
      steps: [
        { id: 1, name: 'areas', label: 'Areas Management', isCompleted: false, isValid: true },
        { id: 2, name: 'subareas', label: 'Subareas Management', isCompleted: false, isValid: false },
        { id: 3, name: 'upload', label: 'Data Upload', isCompleted: false, isValid: false },
        { id: 4, name: 'indicators', label: 'Indicator Review & Management', isCompleted: false, isValid: false },
        { id: 5, name: 'goals', label: 'Goal Management', isCompleted: false, isValid: false },
      ],
      nextStep: jest.fn(),
      setCurrentStep: jest.fn(),
      canProceedToStep: jest.fn(() => true),
    };
    return selector ? selector(state) : state;
  },
}));

import '@testing-library/jest-dom';
import React from 'react';
import { render, screen } from '@testing-library/react';
import WizardPage from '../page';

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

jest.mock('@/store/wizardStore', () => ({
  useWizardStore: () => ({
    dirtyAreas: [],
    dirtySubareas: [],
    saveStep: jest.fn(),
    hasUnsavedChanges: () => false,
    isSaving: false,
    fetchAreas: jest.fn(),
    setStepValid: jest.fn(),
  }),
}));

describe('WizardPage', () => {
  it('renders the wizard page with dynamic step title', () => {
    render(<WizardPage />);
    expect(screen.getByTestId('wizard-step-title')).toHaveTextContent('Step 1: Areas Management');
  });

  it('shows the step subtitle', () => {
    render(<WizardPage />);
    expect(screen.getByTestId('wizard-step-subtitle')).toHaveTextContent('Configure the main areas for your dashboard');
  });

  it('shows the step content list', () => {
    render(<WizardPage />);
    expect(screen.getByText(/Create up to 5 main areas for your dashboard/)).toBeInTheDocument();
    expect(screen.getByText(/Define names and descriptions for each area/)).toBeInTheDocument();
    expect(screen.getByText(/Set up the foundation for your indicator groupings/)).toBeInTheDocument();
  });

  it('has proper semantic structure', () => {
    render(<WizardPage />);
    expect(screen.getByRole('heading', { level: 4 })).toHaveTextContent('Step 1: Areas Management');
  });
}); 