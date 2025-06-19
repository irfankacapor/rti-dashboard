import React from 'react';
import { render, screen } from '@testing-library/react';
import { WizardContainer } from '../WizardContainer';
import { useWizardStore } from '@/lib/store/useWizardStore';

jest.mock('@/lib/store/useWizardStore');
jest.mock('../WizardStepper', () => ({
  WizardStepper: () => <div data-testid="wizard-stepper">Stepper</div>
}));
jest.mock('../WizardNavigation', () => ({
  WizardNavigation: (props: any) => (
    <div data-testid="wizard-navigation">
      Navigation - nextDisabled: {props.nextDisabled ? 'true' : 'false'}
    </div>
  )
}));

const mockUseWizardStore = useWizardStore as jest.MockedFunction<typeof useWizardStore>;

describe('WizardContainer', () => {
  const defaultMockState = {
    currentStep: 1,
    steps: [
      { id: 1, name: 'areas', label: 'Areas Management', isCompleted: false, isValid: true },
    ],
    isLoading: false,
  };

  beforeEach(() => {
    mockUseWizardStore.mockReturnValue(defaultMockState as any);
  });

  it('renders wizard container with stepper and content', () => {
    render(
      <WizardContainer>
        <div data-testid="test-content">Test Content</div>
      </WizardContainer>
    );

    expect(screen.getByTestId('wizard-container')).toBeInTheDocument();
    expect(screen.getByTestId('wizard-stepper')).toBeInTheDocument();
    expect(screen.getByTestId('test-content')).toBeInTheDocument();
  });

  it('displays custom title when provided', () => {
    render(
      <WizardContainer title="Custom Title">
        <div>Content</div>
      </WizardContainer>
    );

    expect(screen.getByTestId('wizard-step-title')).toHaveTextContent('Custom Title');
  });

  it('displays step label as title when no custom title provided', () => {
    render(
      <WizardContainer>
        <div>Content</div>
      </WizardContainer>
    );

    expect(screen.getByTestId('wizard-step-title')).toHaveTextContent('Areas Management');
  });

  it('displays subtitle when provided', () => {
    render(
      <WizardContainer subtitle="Custom Subtitle">
        <div>Content</div>
      </WizardContainer>
    );

    expect(screen.getByTestId('wizard-step-subtitle')).toHaveTextContent('Custom Subtitle');
  });

  it('renders navigation when showNavigation is true', () => {
    render(
      <WizardContainer showNavigation={true}>
        <div>Content</div>
      </WizardContainer>
    );

    expect(screen.getByTestId('wizard-navigation')).toBeInTheDocument();
  });

  it('does not render navigation when showNavigation is false', () => {
    render(
      <WizardContainer showNavigation={false}>
        <div>Content</div>
      </WizardContainer>
    );

    expect(screen.queryByTestId('wizard-navigation')).not.toBeInTheDocument();
  });

  it('passes nextDisabled prop to navigation', () => {
    render(
      <WizardContainer nextDisabled={true}>
        <div>Content</div>
      </WizardContainer>
    );

    expect(screen.getByTestId('wizard-navigation')).toHaveTextContent('nextDisabled: true');
  });

  it('shows step content when not loading', () => {
    render(
      <WizardContainer>
        <div data-testid="step-content">Content</div>
      </WizardContainer>
    );

    expect(screen.getByTestId('wizard-step-content')).toBeInTheDocument();
    expect(screen.getByTestId('step-content')).toBeInTheDocument();
  });
}); 