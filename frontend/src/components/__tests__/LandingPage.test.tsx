import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { useRouter, useParams } from 'next/navigation';
import { NextIntlClientProvider } from 'next-intl';
import LandingPage from '@/app/[locale]/page';
import '@testing-library/jest-dom';

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  useParams: jest.fn(),
}));

const mockPush = jest.fn();
const mockUseRouter = useRouter as jest.MockedFunction<typeof useRouter>;
const mockUseParams = useParams as jest.MockedFunction<typeof useParams>;

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
    mockUseParams.mockReturnValue({ locale: 'en' });
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
    
    const loginButton = screen.getByTestId('login-button');
    const dashboardButton = screen.getByTestId('go-to-dashboard-button');
    expect(loginButton).toBeInTheDocument();
    expect(dashboardButton).toBeInTheDocument();
    expect(loginButton).toHaveTextContent('Login');
    expect(dashboardButton).toHaveTextContent('Go to Dashboard');
  });

  it('navigates to wizard when start button is clicked', () => {
    renderLandingPage();
    
    const dashboardButton = screen.getByTestId('go-to-dashboard-button');
    fireEvent.click(dashboardButton);
    
    expect(mockPush).toHaveBeenCalledWith('/en/dashboard');
  });

  it('has proper semantic structure', () => {
    renderLandingPage();
    
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /go to dashboard/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('RTI Dashboard');
  });

  it('displays the logo with correct styling', () => {
    renderLandingPage();
    
    const logo = screen.getByTestId('logo');
    expect(logo).toBeInTheDocument();
    expect(logo).toHaveTextContent('RTI');
  });
}); 