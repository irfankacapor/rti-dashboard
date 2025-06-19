import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { useRouter } from 'next/navigation';
import { NextIntlClientProvider } from 'next-intl';
import LandingPage from '@/app/[locale]/page';
import '@testing-library/jest-dom';

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

const mockPush = jest.fn();
const mockUseRouter = useRouter as jest.MockedFunction<typeof useRouter>;

const messages = {
  Landing: {
    title: 'RTI Dashboard',
    subtitle: 'Create comprehensive indicator dashboards',
    startButton: 'Start Configuration'
  }
};

const renderLandingPage = () => {
  return render(
    <NextIntlClientProvider messages={messages} locale="en">
      <LandingPage />
    </NextIntlClientProvider>
  );
};

describe('LandingPage', () => {
  beforeEach(() => {
    mockUseRouter.mockReturnValue({
      push: mockPush,
      back: jest.fn(),
      forward: jest.fn(),
      refresh: jest.fn(),
      replace: jest.fn(),
      prefetch: jest.fn(),
    } as any);
    jest.clearAllMocks();
  });

  it('renders the landing page with logo and title', () => {
    renderLandingPage();
    
    expect(screen.getByTestId('logo')).toBeInTheDocument();
    expect(screen.getByText('RTI Dashboard')).toBeInTheDocument();
    expect(screen.getByText(/Create comprehensive indicator dashboards/)).toBeInTheDocument();
  });

  it('renders the start configuration button', () => {
    renderLandingPage();
    
    const startButton = screen.getByTestId('start-configuration-button');
    expect(startButton).toBeInTheDocument();
    expect(startButton).toHaveTextContent('Start Configuration');
  });

  it('navigates to wizard when start button is clicked', () => {
    renderLandingPage();
    
    const startButton = screen.getByTestId('start-configuration-button');
    fireEvent.click(startButton);
    
    expect(mockPush).toHaveBeenCalledWith('/en/wizard');
  });

  it('has proper semantic structure', () => {
    renderLandingPage();
    
    expect(screen.getByRole('button', { name: /start configuration/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('RTI Dashboard');
  });

  it('displays the logo with correct styling', () => {
    renderLandingPage();
    
    const logo = screen.getByTestId('logo');
    expect(logo).toBeInTheDocument();
    expect(logo).toHaveTextContent('RTI');
  });
}); 