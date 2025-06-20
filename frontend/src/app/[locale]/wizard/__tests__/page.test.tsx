import '@testing-library/jest-dom';
import React from 'react';
import { render, screen } from '@testing-library/react';
import WizardPage from '../page';

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